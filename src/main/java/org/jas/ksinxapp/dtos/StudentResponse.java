package org.jas.ksinxapp.dtos;

import java.time.LocalDateTime;

public record StudentResponse(
        Long id,
        String email,
        String fullName,
        String role
) {
}
