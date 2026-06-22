package org.jas.ksinxapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long enrollmentId,
        Long studentId,
        String studentFullName,
        Long courseId,
        String courseTitle,
        LocalDateTime enrollmentDate,
        boolean isActive,
        boolean courseActive,
        int totalTasks,
        int totalCompletedTasks,
        double completionPercentage
) {
}
