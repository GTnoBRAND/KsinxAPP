package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.EnrollmentResponse;
import org.jas.ksinxapp.model.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    @Mapping(source = "id", target = "enrollmentId")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentFullName")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.title", target = "courseTitle")
    @Mapping(target = "totalTasks", constant = "0")
    @Mapping(target = "totalCompletedTasks", constant = "0")
    @Mapping(target = "completionPercentage", expression = "java(0.0)")
    EnrollmentResponse toResponseDto(Enrollment enrollment);

    @Mapping(source = "enrollment.id", target = "enrollmentId")
    @Mapping(source = "enrollment.student.id", target = "studentId")
    @Mapping(source = "enrollment.student.fullName", target = "studentFullName")
    @Mapping(source = "enrollment.course.id", target = "courseId")
    @Mapping(source = "enrollment.course.title", target = "courseTitle")
    @Mapping(target = "totalTasks", source = "totalTasks")
    @Mapping(target = "totalCompletedTasks", source = "totalCompletedTasks")
    @Mapping(target = "completionPercentage", source = "completionPercentage")
    EnrollmentResponse toResponse(Enrollment enrollment, int totalTasks, int totalCompletedTasks, double completionPercentage);
}
