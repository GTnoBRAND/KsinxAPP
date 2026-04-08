package org.jas.ksinxapp.dtos;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(
        @NotNull Long userId,
        @NotNull Long courseId,
        String paymentReference
){
}
