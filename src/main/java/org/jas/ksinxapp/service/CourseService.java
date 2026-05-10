package org.jas.ksinxapp.service;

import jakarta.transaction.Transactional;
import org.jas.ksinxapp.dtos.CourseCreateRequest;
import org.jas.ksinxapp.dtos.CourseResponse;
import org.jas.ksinxapp.mappers.CourseMapper;
import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.repo.CourseRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepo courseRepo;
    private final CourseMapper courseMapper;

    public CourseService(CourseRepo courseRepo, CourseMapper courseMapper) {
        this.courseRepo = courseRepo;
        this.courseMapper = courseMapper;
    }

    @Transactional
    public CourseResponse createResponse(CourseCreateRequest request) {
        //convert dto to entity
        Course course = courseMapper.toEntity(request);

        //save to postgresql
        Course savedCourse = courseRepo.save(course);

        //convert back to dto and return
        return courseMapper.toResponse(savedCourse);
    }

    @Transactional
    public List<CourseResponse> getAllResponse(int pageNo, int pageSize, String sortBy, String sortDir) {

        //handle sorting
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ?Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        //handle pagination
        Pageable pageable = PageRequest.of(pageNo -1, pageSize, sort);

//        //handle dynamic filtering with specification
//        Specification<Course> specification = Specification.where(Specification.unrestricted())
        return courseRepo.findAll(pageable)
                .stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseResponse findById(Long id){
        Course course = courseRepo.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found"));
        return courseMapper.toResponse(course);
    }

    @Transactional
    public CourseResponse updateResponse(Long id, CourseCreateRequest request) {
        Course course = courseRepo.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found"));

        courseMapper.updateEntityFromDto(request, course);

        return courseMapper.toResponseDto(course);
    }

    @Transactional
    public CourseResponse deleteById(Long id, boolean isActive){
        Course course = courseRepo.findById(id)
                .orElseThrow(()->new RuntimeException("Course not found"));

        course.setActive(isActive);
        courseRepo.save(course);

        return courseMapper.toResponseDto(course);
    }
}
