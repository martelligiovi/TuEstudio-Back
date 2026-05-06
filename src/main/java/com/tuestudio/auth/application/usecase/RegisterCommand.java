package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.domain.Role;

public record RegisterCommand(String name, String email, String rawPassword, Role role) {}
