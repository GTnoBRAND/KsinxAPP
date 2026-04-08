package org.jas.ksinxapp.dtos;

import java.time.LocalDateTime;

public record TaskSubmissionResponse(
        Long id,
        Long taskId,
        String taskTitle,
        Long studentId,
        String studentFullName,
        String fileUrl,
        LocalDateTime submittedAt,
        Integer score,
        String teacherFeedback
) {
}
