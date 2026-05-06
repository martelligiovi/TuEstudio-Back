package com.tuestudio.auth.infrastructure.web;

import com.tuestudio.auth.application.usecase.LoginCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}
