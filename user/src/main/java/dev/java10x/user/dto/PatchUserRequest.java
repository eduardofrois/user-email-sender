package dev.java10x.user.dto;

import jakarta.validation.constraints.Email;

public record PatchUserRequest(
        String name,

        @Email(message = "Email deve ser válido")
        String email
) {
}
