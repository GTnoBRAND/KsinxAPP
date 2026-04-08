package org.jas.ksinxapp.controller;

import jakarta.validation.Valid;
import org.jas.ksinxapp.dtos.CourseCreateRequest;
import org.jas.ksinxapp.dtos.CourseResponse;
import org.jas.ksinxapp.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping("/all")
    public ResponseEntity<List<CourseResponse>> findAll() {
        return ResponseEntity.ok(service.getAllCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> findById(@PathVariable Long id){
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CourseResponse> update(@PathVariable @Valid Long id, @RequestBody CourseCreateRequest request){
        return ResponseEntity.ok(service.updateResponse(id, request));
    }

    @PostMapping("/add")
    public ResponseEntity<CourseResponse> add(@Valid @RequestBody CourseCreateRequest request){
        return ResponseEntity.ok(service.createResponse(request));
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable @Valid Long id){
        service.deleteById(id);
    }
}
