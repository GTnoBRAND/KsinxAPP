package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.ModulesRequest;
import org.jas.ksinxapp.dtos.ModulesResponse;
import org.jas.ksinxapp.model.Modules;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModulesMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    Modules toEntity(ModulesRequest modulesRequest);

    @Mapping(source = "course.id", target = "courseId")
    ModulesResponse toResponse(Modules modules);
}
