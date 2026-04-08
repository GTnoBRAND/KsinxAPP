package org.jas.ksinxapp.dtos;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;


//what frontend sends to us
public record CourseCreateRequest(
        @NotBlank String title,
        @NotBlank String description,
        BigDecimal price
) {
}
