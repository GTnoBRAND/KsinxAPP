package org.jas.ksinxapp.dtos;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

//what frontend gets from backend
public record CourseResponse(
        @NotBlank Long id,
        @NotBlank String title,
        @NotBlank String description,
        BigDecimal price,
        int totalModules
) {
}
