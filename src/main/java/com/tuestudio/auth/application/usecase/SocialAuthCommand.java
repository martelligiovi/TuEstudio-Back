package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.domain.AuthProvider;
import com.tuestudio.auth.domain.Role;
import java.util.Optional;

public record SocialAuthCommand(
        String name,
        String email,
        String providerUserId,
        AuthProvider provider,
        Optional<Role> role
) {}
