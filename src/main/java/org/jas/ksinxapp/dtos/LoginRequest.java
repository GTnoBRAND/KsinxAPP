package org.jas.ksinxapp.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String fullName,
        @NotBlank String password
) {
}
