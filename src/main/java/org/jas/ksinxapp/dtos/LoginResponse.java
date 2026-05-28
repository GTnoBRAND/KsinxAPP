package org.jas.ksinxapp.dtos;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record LoginResponse(
        Long id,
        String token,
        String fullName,
        String role,
        Collection<? extends GrantedAuthority> authorities
) {
}
