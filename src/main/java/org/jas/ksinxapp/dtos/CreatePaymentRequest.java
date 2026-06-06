package org.jas.ksinxapp.dtos;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(
        @NotNull Long courseId
) {}