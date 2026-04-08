package org.jas.ksinxapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.dtos.EnrollmentRequest;
import org.jas.ksinxapp.dtos.EnrollmentResponse;
import org.jas.ksinxapp.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vi/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enrollStudent(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enrollStudent(request);

        //return 201 created instead of 200 ok
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }
}
