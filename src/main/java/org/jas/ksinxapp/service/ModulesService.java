package org.jas.ksinxapp.service;

import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.dtos.ModulesRequest;
import org.jas.ksinxapp.dtos.ModulesResponse;
import org.jas.ksinxapp.mappers.ModulesMapper;
import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.model.Modules;
import org.jas.ksinxapp.repo.CourseRepo;
import org.jas.ksinxapp.repo.ModulesRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModulesService {

    private final ModulesRepo modulesRepo;
    private final ModulesMapper modulesMapper;
    private final CourseRepo courseRepo;
    private final MinIoStorageService minIoStorageService;

    @Transactional
    //create a new lesson in a course
    public ModulesResponse createModule(ModulesRequest request, MultipartFile video) {

        //fetch the actual course entity
        Course course = courseRepo.findById(request.courseId())
                .orElseThrow(() -> new RuntimeException("Can not create module: course with id:" + request.courseId() + " not found"));
        //use mapstruct to map the flat fields
        Modules newModule = modulesMapper.toEntity(request);

        //manually attach managed course entity we just fetched
        newModule.setCourse(course);
        if(video != null && !video.isEmpty()){
            newModule.setVideoUrl(minIoStorageService.publicUpload(video));
        }

        //save to postgresql
        Modules savedModule = modulesRepo.save(newModule);

        //return dto response
        return modulesMapper.toResponse(savedModule);
    }

    @Transactional(readOnly = true)
    public List<ModulesResponse> getModulesForCourse(Long courseId) {
        //check if the course exists first

        if (!courseRepo.existsById(courseId)) {
            throw new RuntimeException("Course not found!");
        }

        //custom repo method to order in asc
        List<Modules> modules = modulesRepo.findByCourseIdAndIsActiveTrueOrderBySequenceOrderAsc(courseId);

        //map them to response dto
        return modules.stream()
                .map(modulesMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ModulesResponse updateModuleStatus(Long moduleId, boolean isActive){
        Modules modules = modulesRepo.findById(moduleId)
                .orElseThrow(()->new RuntimeException("Module not found"));

        modules.setActive(isActive);
        modulesRepo.save(modules);
        return modulesMapper.toResponse(modules);
    }

}
