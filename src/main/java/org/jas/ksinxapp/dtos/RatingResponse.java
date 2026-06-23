package org.jas.ksinxapp.dtos;

public record RatingResponse(
        Long courseId,
        Double averageRating,
        Long ratingCount,
        Integer myRating
) {
}
