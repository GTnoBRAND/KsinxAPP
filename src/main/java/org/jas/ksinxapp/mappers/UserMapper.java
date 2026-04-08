package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.StudentRegistrationRequest;
import org.jas.ksinxapp.dtos.StudentResponse;
import org.jas.ksinxapp.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    //map request to entity, but let the service to handle sensitive stuff!
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    User toEntity(StudentRegistrationRequest request);

    //map entity to response
    StudentResponse toResponse(User user);

    //update request dto to entity
    void updateEntityFromDto(StudentRegistrationRequest request, @MappingTarget User user);
}
