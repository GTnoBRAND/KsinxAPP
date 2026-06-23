package org.jas.ksinxapp.controller;

import jakarta.validation.Valid;
import org.jas.ksinxapp.dtos.CourseCreateRequest;
import org.jas.ksinxapp.dtos.CourseResponse;
import org.jas.ksinxapp.dtos.RatingRequest;
import org.jas.ksinxapp.dtos.RatingResponse;
import org.jas.ksinxapp.model.CourseCategory;
import org.jas.ksinxapp.service.CourseRatingService;
import org.jas.ksinxapp.service.CourseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    private final CourseService service;
    private final CourseRatingService ratingService;

    public CourseController(CourseService service, CourseRatingService ratingService) {
        this.service = service;
        this.ratingService = ratingService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<CourseResponse>> findAll(@RequestParam(required = false, defaultValue = "1") int pageNo,
                                                        @RequestParam(required = false, defaultValue = "5") int pageSize,
                                                        @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                        @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                                        @RequestParam(required = false) CourseCategory category,
                                                        @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(service.getAllResponse(pageNo, pageSize, sortBy, sortDir, category, includeInactive));
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<CourseResponse> findById(@PathVariable Long id){
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseResponse> update(@PathVariable Long id,
                                                 @RequestParam String title,
                                                 @RequestParam String description,
                                                 @RequestParam BigDecimal price,
                                                 @RequestParam(required = false) CourseCategory category,
                                                 @RequestParam(required = false) MultipartFile image,
                                                 @RequestParam(required = false) MultipartFile video){
        CourseCreateRequest request = new CourseCreateRequest(title, description, price, category);
        return ResponseEntity.ok(service.updateResponse(id, request, image, video));
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseResponse> add(@RequestParam String title,
                                              @RequestParam String description,
                                              @RequestParam BigDecimal price,
                                              @RequestParam(required = false) CourseCategory category,
                                              @RequestParam(required = false) MultipartFile image,
                                              @RequestParam(required = false) MultipartFile video){
        CourseCreateRequest request = new CourseCreateRequest(title, description, price, category);
        return ResponseEntity.ok(service.createResponse(request, image, video));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CourseResponse> delete(@PathVariable @Valid Long id,
                                                 @RequestParam boolean isActive){
        return ResponseEntity.ok(service.setActive(id, isActive));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CourseResponse> setStatus(@PathVariable Long id,
                                                    @RequestParam boolean isActive){
        return ResponseEntity.ok(service.setActive(id, isActive));
    }

    @PostMapping("/{id}/rate")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RatingResponse> rate(@PathVariable Long id, @RequestBody RatingRequest body){
        return ResponseEntity.ok(ratingService.rateCourse(id, body.rating()));
    }

    @GetMapping("/{id}/rating")
    public ResponseEntity<RatingResponse> rating(@PathVariable Long id){
        return ResponseEntity.ok(ratingService.getRating(id));
    }
}
