package org.jas.ksinxapp.service;

import org.jas.ksinxapp.dtos.ModulesRequest;
import org.jas.ksinxapp.dtos.ModulesResponse;
import org.jas.ksinxapp.mappers.ModulesMapper;
import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.model.Modules;
import org.jas.ksinxapp.repo.CourseRepo;
import org.jas.ksinxapp.repo.ModulesRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModulesService {

    private final ModulesRepo modulesRepo;
    private final ModulesMapper modulesMapper;
    private final CourseRepo courseRepo;

    public ModulesService(ModulesRepo modulesRepo, ModulesMapper modulesMapper, CourseRepo courseRepo) {
        this.modulesRepo = modulesRepo;
        this.modulesMapper = modulesMapper;
        this.courseRepo = courseRepo;
    }
    @Transactional
    //create a new lesson in a course
    public ModulesResponse createModule(ModulesRequest request) {

        //fetch the actual course entity
        Course course = courseRepo.findById(request.courseId())
                .orElseThrow(() -> new RuntimeException("Can not create module: course with id:" + request.courseId() + " not found"));
        //use mapstruct to map the flat fields
        Modules newModule = modulesMapper.toEntity(request);

        //manually attach managed course entity we just fetched
        newModule.setCourse(course);

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
        List<Modules> modules = modulesRepo.findByCourseIdOrderBySequenceOrderAsc(courseId);

        //map them to response dto
        return modules.stream()
                .map(modulesMapper::toResponse)
                .collect(Collectors.toList());
    }

}
