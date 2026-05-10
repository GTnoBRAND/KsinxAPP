package org.jas.ksinxapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.dtos.TaskSubmissionRequest;
import org.jas.ksinxapp.dtos.TaskSubmissionResponse;
import org.jas.ksinxapp.service.TaskSubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/submission")
@RequiredArgsConstructor
public class TaskSubmissionController {

    private final TaskSubmissionService service;

    @PostMapping("/submit")
    public ResponseEntity<TaskSubmissionResponse> submitTask(@Valid @RequestBody TaskSubmissionRequest taskSubmissionRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submitTask(taskSubmissionRequest));
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

}
