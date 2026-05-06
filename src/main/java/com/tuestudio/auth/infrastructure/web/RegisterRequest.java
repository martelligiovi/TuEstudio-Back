package com.tuestudio.auth.infrastructure.web;

import com.tuestudio.auth.application.usecase.RegisterCommand;
import com.tuestudio.auth.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8) String password,
        @NotNull Role role
) {
    public RegisterCommand toCommand() {
        return new RegisterCommand(name, email, password, role);
    }
}
