package org.jas.ksinxapp.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


//what frontend sends to me(when a new student signed up)
public record StudentRegistrationRequest(
        @NotBlank(message = "Full name is required!")
        String fullName,
        @Email(message = "Must be a valid email address!")
        @NotBlank(message = "Email is required!")
        String email,
        @NotBlank(message = "Password required!")
        @Size(min = 8, message = "Password must be at least 8 characters!")
        String password
) {
}
