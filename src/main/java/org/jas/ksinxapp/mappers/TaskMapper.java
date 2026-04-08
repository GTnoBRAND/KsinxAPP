package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.TaskRequest;
import org.jas.ksinxapp.dtos.TaskResponse;
import org.jas.ksinxapp.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "module", ignore = true)
    Task toEntity(TaskRequest request);

    @Mapping(source = "module.id",  target = "moduleId")
    TaskResponse toResponse(Task task);
}
