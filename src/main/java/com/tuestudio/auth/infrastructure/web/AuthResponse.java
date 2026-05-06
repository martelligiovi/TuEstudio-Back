package com.tuestudio.auth.infrastructure.web;

import com.tuestudio.auth.application.usecase.AuthResult;
import com.tuestudio.auth.domain.Role;
import java.util.UUID;

public record AuthResponse(String token, UUID userId, String name, String email, Role role) {
    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.token(), result.userId(), result.name(), result.email(), result.role());
    }
}
