package org.jas.ksinxapp.service;

import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.dtos.TaskSubmissionResponse;
import org.jas.ksinxapp.mappers.TaskSubmissionMapper;
import org.jas.ksinxapp.model.Task;
import org.jas.ksinxapp.model.TaskSubmission;
import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.repo.TaskRepo;
import org.jas.ksinxapp.repo.TaskSubmissionRepo;
import org.jas.ksinxapp.repo.UserRepo;
import org.jas.ksinxapp.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskSubmissionService {

    private final TaskSubmissionRepo taskSubmissionRepo;
    private final TaskRepo taskRepo;
    private final UserRepo userRepo;
    private final TaskSubmissionMapper taskSubmissionMapper;
    private final MinIoStorageService minIoStorageService;


    //a student uploads a task
    @Transactional
    public TaskSubmissionResponse submitTask(Long studentId, Long taskId, MultipartFile file){
        User student = userRepo.findById(studentId)
                .orElseThrow(()->new RuntimeException("Student id not found"));

        Task task = taskRepo.findById(taskId)
                .orElseThrow(()->new RuntimeException("Task id not found"));

        if(taskSubmissionRepo.existsByStudentIdAndTaskId(student.getId(), task.getId())){
            throw new RuntimeException("You have already submitted this task");
        }

        // backend uploads to the PRIVATE bucket and gets the permanent object key
        String fileKey = minIoStorageService.privateUpload(file);

        //we build the entity manually here as we need actual User and Task objects
        TaskSubmission taskSubmission = TaskSubmission.builder()
                .task(task)
                .student(student)
                .fileKey(fileKey)
                .build();

        TaskSubmission savedSubmission = taskSubmissionRepo.save(taskSubmission);
        return taskSubmissionMapper.toResponse(savedSubmission);
    }

    //a teacher grades the homework
    @Transactional
    public TaskSubmissionResponse gradeSubmission(Long submissionId, Integer score, String feedback){
        TaskSubmission submission = taskSubmissionRepo.findById(submissionId)
                .orElseThrow(()->new RuntimeException("Submission id not found"));
        if(submission.getScore()!=null){
            throw new RuntimeException("Submission already graded!");
        }

        submission.setScore(score);
        submission.setTeacherFeedback(feedback);

        TaskSubmission savedSubmission = taskSubmissionRepo.save(submission);
        return taskSubmissionMapper.toResponse(savedSubmission);
    }

    @Transactional(readOnly = true)
    public TaskSubmissionResponse getFeedback(Long submissionId, Long callerId, boolean callerIsTeacher){
        TaskSubmission submission = taskSubmissionRepo.findById(submissionId)
                .orElseThrow(()->new RuntimeException("Submission not found"));
        //authorization, only student and a teacher can see it
        boolean isOwner = submission.getStudent().getId().equals(callerId);
        if(!isOwner && !callerIsTeacher){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this submission feedback");
        }

        if(submission.getScore() == null){
            throw new RuntimeException("This submission has not been graded yet!");
        }
        return taskSubmissionMapper.toResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<TaskSubmissionResponse> getStudentGradedSubmissions(Long studentId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long loggedInUser = principal.getUser().getId();

        if(!loggedInUser.equals(studentId)){
            throw new RuntimeException("You can only view your own submissions!");
        }

        return taskSubmissionRepo.findByStudentIdAndScoreIsNotNull(studentId)
                .stream()
                .map(taskSubmissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskSubmissionResponse> getUngradedSubmission(){
        return taskSubmissionRepo.findByScoreIsNull()
                .stream()
                .map(taskSubmissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getSubmissionFileUrl(Long submissionId, Long callerId, boolean callerIsTeacher){
        TaskSubmission submission = taskSubmissionRepo.findById(submissionId)
                .orElseThrow(()->new RuntimeException("Submission Id not found!"));

        //authorization
        //allow IF caller owns the submission OR is a teacher
        boolean isOwner = submission.getStudent().getId().equals(callerId);
        if(!isOwner && !callerIsTeacher){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this file");
        }

        return minIoStorageService.generatePreSignedUrl(submission.getFileKey());
    }
    
}
