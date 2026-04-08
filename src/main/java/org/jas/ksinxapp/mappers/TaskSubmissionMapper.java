package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.TaskSubmissionResponse;
import org.jas.ksinxapp.model.TaskSubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskSubmissionMapper {

    // We don't map the Request to Entity here because we need to fetch the User and Task manually in the Service
    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "task.title", target = "taskTitle")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentFullName")
    TaskSubmissionResponse toResponse(TaskSubmission taskSubmission);
}
