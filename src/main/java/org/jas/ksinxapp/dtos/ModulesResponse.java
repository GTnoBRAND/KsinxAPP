package org.jas.ksinxapp.dtos;

public record ModulesResponse(
        Long id,
        String title,
        Integer sequenceOrder,
        String videoUrl,
        Long courseId
) {
}
