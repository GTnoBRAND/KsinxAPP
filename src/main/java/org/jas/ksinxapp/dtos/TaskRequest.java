package org.jas.ksinxapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskRequest(
        @NotBlank String title,
        @NotBlank String instructions,
        @NotNull Integer maxScore,
        LocalDateTime dueDate,
        @NotNull Long moduleId
) {
}
