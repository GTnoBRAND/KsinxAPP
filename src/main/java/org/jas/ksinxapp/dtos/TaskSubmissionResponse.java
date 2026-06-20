package org.jas.ksinxapp.dtos;

import java.time.LocalDateTime;

public record TaskSubmissionResponse(
        Long id,
        Long taskId,
        String taskTitle,
        Long studentId,
        String studentFullName,
        String fileKey,
        LocalDateTime submittedAt,
        Integer score,
        String teacherFeedback
) {
}
