package com.tuestudio.auth.infrastructure.persistence;

import com.tuestudio.auth.application.usecase.PasswordHasher;
import com.tuestudio.auth.domain.HashedPassword;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Dev-only seeder. Creates one ADMIN user if none exists.
 * Idempotent: no-op if an ADMIN already exists.
 * NOT active in test profile — use the register endpoint or direct repo access in tests.
 */
@Component
@Profile("!test")
class AdminSeeder {

    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private static final String ADMIN_EMAIL = "admin@tuestudio.dev";

    private final UserJpaRepository userRepository;
    private final String hashedPassword;

    AdminSeeder(UserJpaRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.hashedPassword = passwordHasher.hash("AdminDev123!");
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    void seed() {
        if (userRepository.existsByRole(Role.ADMIN)) return;

        User admin = new User(ADMIN_ID, "Admin", ADMIN_EMAIL, new HashedPassword(hashedPassword), Role.ADMIN);
        userRepository.save(UserJpaEntity.fromDomain(admin));
    }
}
