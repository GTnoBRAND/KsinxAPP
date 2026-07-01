package org.jas.ksinxapp.controller;

import jakarta.validation.Valid;
import org.jas.ksinxapp.dtos.ModulesRequest;
import org.jas.ksinxapp.dtos.ModulesResponse;
import org.jas.ksinxapp.service.ModulesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/modules")
public class ModulesController {

    private final ModulesService modulesService;

    public ModulesController(ModulesService modulesService) {
        this.modulesService = modulesService;
    }

    @PostMapping("/create")
    public ResponseEntity<ModulesResponse> createModule(@Valid @RequestBody ModulesRequest request,
                                                        @RequestParam(required = false) MultipartFile video) {
        ModulesResponse response = modulesService.createModule(request, video);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ModulesResponse>> getModulesForCourse(@PathVariable Long courseId) {
        List<ModulesResponse> response = modulesService.getModulesForCourse(courseId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/{moduleId}/status")
    public ResponseEntity<ModulesResponse> updateModulesStatus(
            @PathVariable Long moduleId,
            @RequestParam boolean isActive
    ){
        return ResponseEntity.ok(modulesService.updateModuleStatus(moduleId, isActive));
    }
}
