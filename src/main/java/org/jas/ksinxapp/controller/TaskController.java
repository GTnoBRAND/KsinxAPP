package org.jas.ksinxapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.dtos.TaskRequest;
import org.jas.ksinxapp.dtos.TaskResponse;
import org.jas.ksinxapp.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;


    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(taskRequest));
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<TaskResponse>> getTasksForModule(@PathVariable Long moduleId) {

        return ResponseEntity.ok(taskService.getTasksForModule(moduleId));
    }
}
