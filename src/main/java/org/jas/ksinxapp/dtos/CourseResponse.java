package org.jas.ksinxapp.dtos;

import jakarta.validation.constraints.NotBlank;
import org.jas.ksinxapp.model.CourseCategory;

import java.io.Serializable;
import java.math.BigDecimal;

//what frontend gets from backend
public record CourseResponse(
        @NotBlank Long id,
        @NotBlank String title,
        @NotBlank String description,
        BigDecimal price,
        String imageUrl,
        String videoUrl,
        int totalModules,
        CourseCategory category,
        Boolean isActive,
        Double averageRating,
        Long ratingCount
) implements Serializable {
}
