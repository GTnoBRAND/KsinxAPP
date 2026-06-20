package org.jas.ksinxapp.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.dtos.TaskSubmissionRequest;
import org.jas.ksinxapp.dtos.TaskSubmissionResponse;
import org.jas.ksinxapp.model.TaskSubmission;
import org.jas.ksinxapp.model.User;
import org.jas.ksinxapp.security.UserPrincipal;
import org.jas.ksinxapp.service.TaskSubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/submission")
@RequiredArgsConstructor
public class TaskSubmissionController {

    private final TaskSubmissionService service;

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TaskSubmissionResponse> submitTask(Authentication auth,
                                                             @RequestParam Long taskId,
                                                             @RequestParam MultipartFile file) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long studentId = principal.getUser().getId();

        return ResponseEntity.status(HttpStatus.CREATED).body(service.submitTask(studentId, taskId, file));
    }

    //put, teacher submits a grade
    @PutMapping("/{id}/grade")
    public ResponseEntity<TaskSubmissionResponse> gradeSubmission(@PathVariable Long id, @RequestParam Integer score, @RequestParam String teacherFeedback){
        return ResponseEntity.ok(service.gradeSubmission(id, score, teacherFeedback));
    }

    //get: teacher checks for new homework
    @GetMapping("/ungraded")
    public ResponseEntity<List<TaskSubmissionResponse>> getUngradedTasks(){
        return ResponseEntity.ok(service.getUngradedSubmission());
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<String> getSubmissionFile(@PathVariable Long id, Authentication auth){
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long callerId = principal.getUser().getId();
        boolean isTeacher = principal.getUser().getRole() == User.Role.TEACHER
                || principal.getUser().getRole() ==User.Role.ADMIN;

        String url = service.getSubmissionFileUrl(id, callerId, isTeacher);
        return ResponseEntity.ok(url);
    }

}
