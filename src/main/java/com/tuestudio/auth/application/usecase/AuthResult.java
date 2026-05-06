package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.domain.Role;
import java.util.UUID;

public record AuthResult(String token, UUID userId, String name, String email, Role role) {}
