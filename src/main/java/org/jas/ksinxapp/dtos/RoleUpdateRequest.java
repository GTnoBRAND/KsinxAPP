package org.jas.ksinxapp.dtos;

import jakarta.validation.constraints.NotNull;
import org.jas.ksinxapp.model.User;

public record RoleUpdateRequest(
        @NotNull(message = "Role is required (ADMIN, TEACHER, or STUDENT)")
        User.Role role
) {
}
