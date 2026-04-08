package org.jas.ksinxapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record TaskSubmissionRequest(
        @NotNull Long taskId,
        @NotNull Long studentId,
        @NotBlank String fileUrl
) {
}
