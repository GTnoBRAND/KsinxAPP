package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.CourseCreateRequest;
import org.jas.ksinxapp.model.Course;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
public interface CourseMapper {
    //map an incoming dto to entity(ready to save to db)
    //we ignore id and modules cause it is brand new course
    @Mapping(target = "id",  ignore = true)
    @Mapping(target = "modules",  ignore = true)
    @Mapping(target = "imageUrl",  ignore = true)
    @Mapping(target = "videoUrl",  ignore = true)
    Course toEntity(CourseCreateRequest request);

    //update existing entity from request — null category leaves the existing value alone
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CourseCreateRequest request, @MappingTarget Course course);
}
