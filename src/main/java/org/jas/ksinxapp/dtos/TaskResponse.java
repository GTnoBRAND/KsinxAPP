package org.jas.ksinxapp.dtos;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String instructions,
        Integer maxScore,
        LocalDateTime dueDate,
        Long moduleId
) {
}
