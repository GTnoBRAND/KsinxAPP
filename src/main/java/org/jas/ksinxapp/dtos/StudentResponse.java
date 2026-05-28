package org.jas.ksinxapp.dtos;


public record StudentResponse(
        Long id,
        String email,
        String fullName,
        String role
) {
}
