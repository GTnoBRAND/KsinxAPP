package org.jas.ksinxapp.service;

import org.jas.ksinxapp.dtos.TaskSubmissionRequest;
import org.jas.ksinxapp.dtos.TaskSubmissionResponse;
import org.jas.ksinxapp.mappers.TaskSubmissionMapper;
import org.jas.ksinxapp.model.Task;
import org.jas.ksinxapp.model.TaskSubmission;
import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.repo.TaskRepo;
import org.jas.ksinxapp.repo.TaskSubmissionRepo;
import org.jas.ksinxapp.repo.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskSubmissionService {

    private final TaskSubmissionRepo taskSubmissionRepo;
    private final TaskRepo taskRepo;
    private final UserRepo userRepo;
    private final TaskSubmissionMapper taskSubmissionMapper;

    public TaskSubmissionService(TaskSubmissionRepo taskSubmissionRepo, TaskRepo taskRepo, UserRepo userRepo, TaskSubmissionMapper taskSubmissionMapper) {
        this.taskSubmissionRepo = taskSubmissionRepo;
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
        this.taskSubmissionMapper = taskSubmissionMapper;
    }

    //a student uploads a task
    @Transactional
    public TaskSubmissionResponse submitTask(TaskSubmissionRequest request){
        User student = userRepo.findById(request.studentId())
                .orElseThrow(()->new RuntimeException("Student id not found"));

        Task task = taskRepo.findById(request.taskId())
                .orElseThrow(()->new RuntimeException("Task id not found"));

        //we build the entity manually here as we need actual User and Task objects
        TaskSubmission taskSubmission = new TaskSubmission();
        taskSubmission.setStudent(student);
        taskSubmission.setTask(task);
        taskSubmission.setFileUrl(request.fileUrl());
        taskSubmission.setSubmittedAt(LocalDateTime.now());

        TaskSubmission savedSubmission = taskSubmissionRepo.save(taskSubmission);
        return taskSubmissionMapper.toResponse(savedSubmission);
    }

    //a teacher grades the homework
    @Transactional
    public TaskSubmissionResponse gradeSubmission(Long submissionId, Integer score, String feedback){
        TaskSubmission submission = taskSubmissionRepo.findById(submissionId)
                .orElseThrow(()->new RuntimeException("Submission id not found"));

        submission.setScore(score);
        submission.setTeacherFeedback(feedback);

        TaskSubmission savedSubmission = taskSubmissionRepo.save(submission);
        return taskSubmissionMapper.toResponse(savedSubmission);
    }

    @Transactional(readOnly = true)
    public List<TaskSubmissionResponse> getUngradedSubmission(){
        return taskSubmissionRepo.findByScoreIsNull()
                .stream()
                .map(taskSubmissionMapper::toResponse)
                .collect(Collectors.toList());
    }
    
}
