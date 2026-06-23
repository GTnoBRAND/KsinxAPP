package org.jas.ksinxapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.dtos.CourseProgressResponse;
import org.jas.ksinxapp.dtos.CourseResponse;
import org.jas.ksinxapp.dtos.EnrollmentRequest;
import org.jas.ksinxapp.dtos.EnrollmentResponse;
import org.jas.ksinxapp.mappers.EnrollmentMapper;
import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.model.Enrollment;
import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.repo.*;
import org.jas.ksinxapp.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);
    private final EnrollmentRepo enrollmentRepo;
    private final CourseRepo courseRepo;
    private final UserRepo userRepo;
    private final EnrollmentMapper  enrollmentMapper;
    private final TaskRepo taskRepo;
    private final TaskSubmissionRepo taskSubmissionRepo;

    @Transactional
    public EnrollmentResponse enrollStudent(EnrollmentRequest request){
        User student = userRepo.findById(request.userId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));

        Course course = courseRepo.findById(request.courseId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course Not Found"));

        if(!Boolean.TRUE.equals(course.getIsActive())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This course is currently inactive and cannot be enrolled in until it is reactivated.");
        }

        //prevent duplicate active enrollments(business logic)
        if (enrollmentRepo.existsByStudentIdAndCourseIdAndIsActiveTrue(student.getId(), course.getId())){
            throw new RuntimeException("Student is already enrolled to this course!");
        }
        //manually assemble the new enrollment entity
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .isActive(true)
                .build();

        //save to postgresql
        Enrollment savedEnrollment = enrollmentRepo.save(enrollment);

        //convert to dto and return
        return enrollmentMapper.toResponseDto(savedEnrollment);
    }

    @Transactional
    public List<EnrollmentResponse> getAllEnrollments(){
        return enrollmentRepo.findAll()
                .stream()
                .map(enrollmentMapper::toResponseDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public List<EnrollmentResponse> getMyEnrollments(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long userId = principal.getUser().getId();

        return enrollmentRepo.findByStudentIdAndIsActiveTrue(userId)
                .stream()
                .map(enrollment -> {
                    Long courseId = enrollment.getCourse().getId();
                    int totalTasks = taskRepo.countByModule_CourseId(courseId);
                    int completedTasks = taskSubmissionRepo.countByStudentIdAndTask_Module_CourseIdAndScoreNotNull(userId, courseId);
                    double percentage = totalTasks == 0 ? 0.0 : (double) completedTasks / totalTasks * 100;
                    return enrollmentMapper.toResponse(enrollment, totalTasks, completedTasks, percentage);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseProgressResponse getCourseProgress(Long studentId, Long courseId){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long loggedInUserId = principal.getUser().getId();

        if(!loggedInUserId.equals(studentId)){
            throw new RuntimeException("You can only view your own progress!");
        }

        User student = userRepo.findById(studentId)
                .orElseThrow(()->new UsernameNotFoundException("Student not found"));
        Course course = courseRepo.findById(courseId)
                .orElseThrow(()->new RuntimeException("Course not found"));

        if(!enrollmentRepo.existsByStudentIdAndCourseIdAndIsActiveTrue(studentId, courseId)) {
            throw new RuntimeException("User not enrolled to this course!");
        }

        int totalTask = taskRepo.countByModule_CourseId(courseId);
        int totalCompletedTasks = taskSubmissionRepo.countByStudentIdAndTask_Module_CourseIdAndScoreNotNull(studentId, courseId);

        double percentage = totalTask == 0 ? 0.0 : (double) totalCompletedTasks / totalTask * 100;

        CourseProgressResponse progress = new CourseProgressResponse(
                student.getId(),
                courseId,
                course.getTitle(),
                totalTask,
                totalCompletedTasks,
                percentage
        );

        return progress;
    }
}
