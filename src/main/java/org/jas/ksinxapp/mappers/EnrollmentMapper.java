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
    EnrollmentResponse toResponseDto(Enrollment enrollment);
}
