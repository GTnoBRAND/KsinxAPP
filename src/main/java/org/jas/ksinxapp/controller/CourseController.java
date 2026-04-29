package org.jas.ksinxapp.controller;

import jakarta.validation.Valid;
import org.jas.ksinxapp.dtos.CourseCreateRequest;
import org.jas.ksinxapp.dtos.CourseResponse;
import org.jas.ksinxapp.service.CourseService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<List<CourseResponse>> findAll(@RequestParam(required = false, defaultValue = "1") int pageNo,
                                                        @RequestParam(required = false, defaultValue = "5") int pageSize,
                                                        @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                        @RequestParam(required = false, defaultValue = "asc") String sortDir) {
//        PageRequest pageRequest = PageRequest.of(pageNo -1, pageSize, Sort.by(Sort.Direction.ASC, "name"));

        return ResponseEntity.ok(service.getAllResponse(pageNo, pageSize, sortBy, sortDir));
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
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CourseResponse> add(@Valid @RequestBody CourseCreateRequest request){
        return ResponseEntity.ok(service.createResponse(request));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void delete(@PathVariable @Valid Long id){
        service.deleteById(id);
    }
}
