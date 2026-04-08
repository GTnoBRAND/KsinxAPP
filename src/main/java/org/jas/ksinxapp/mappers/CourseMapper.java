package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.CourseCreateRequest;
import org.jas.ksinxapp.dtos.CourseResponse;
import org.jas.ksinxapp.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface CourseMapper {
    //map an incoming dto to entity(ready to save to db)
    //we ignore id and modules cause it is brand new course
    @Mapping(target = "id",  ignore = true)
    @Mapping(target = "modules",  ignore = true)
    Course toEntity(CourseCreateRequest request);


    //map a saved entity back to response dto for frontend
    @Mapping(target = "totalModules", expression = "java(course.getModules() != null ? course.getModules().size() : 0)")
    CourseResponse toResponse(Course course);

    //update(request dto -> existing entity)
    void updateEntityFromDto(CourseCreateRequest request, @MappingTarget Course course);

    //entity -> response dto
    CourseResponse toResponseDto(Course course);


}
