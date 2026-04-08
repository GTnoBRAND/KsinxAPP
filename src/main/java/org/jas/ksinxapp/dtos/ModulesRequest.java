package org.jas.ksinxapp.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ModulesRequest(
        @NotBlank String title,
        @NotNull Integer sequenceOrder,
        String videoUrl,
        @NotNull Long courseId
) {
}
