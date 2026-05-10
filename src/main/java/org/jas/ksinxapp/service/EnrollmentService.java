package org.jas.ksinxapp.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.dtos.EnrollmentRequest;
import org.jas.ksinxapp.dtos.EnrollmentResponse;
import org.jas.ksinxapp.mappers.EnrollmentMapper;
import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.model.Enrollment;
import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.repo.CourseRepo;
import org.jas.ksinxapp.repo.EnrollmentRepo;
import org.jas.ksinxapp.repo.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepo enrollmentRepo;
    private final CourseRepo courseRepo;
    private final UserRepo userRepo;
    private final EnrollmentMapper  enrollmentMapper;

    @Transactional
    public EnrollmentResponse enrollStudent(EnrollmentRequest request){
        User student = userRepo.findById(request.userId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found"));

        Course course = courseRepo.findById(request.courseId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course Not Found"));

        if(!course.isActive()){
            throw new RuntimeException("Course is not active anymore");
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
}
