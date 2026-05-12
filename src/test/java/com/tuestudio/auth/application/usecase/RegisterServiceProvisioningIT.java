package com.tuestudio.auth.application.usecase;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.TutorId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static com.tuestudio.auth.domain.Role.STUDENT;
import static com.tuestudio.auth.domain.Role.TEACHER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test: register a TEACHER user and verify
 * that both the User and the Tutor stub are persisted with the same UUID.
 *
 * Tests atomicity (User + Tutor in one transaction) via Spring Boot full context.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RegisterServiceProvisioningIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired RegisterUseCase registerUseCase;
    @Autowired TutorRepositoryPort tutorRepository;

    @Test
    void registerTeacher_createsBothUserAndTutorWithSameUUID() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        RegisterCommand cmd = new RegisterCommand(
                "Teacher IT", "teacher-it-" + unique + "@test.com", "secret123", TEACHER);

        AuthResult result = registerUseCase.register(cmd);

        UUID userId = result.userId();
        assertThat(userId).isNotNull();

        // Tutor must exist with the same UUID
        assertThat(tutorRepository.findById(TutorId.of(userId)))
                .isPresent()
                .hasValueSatisfying(tutor -> {
                    assertThat(tutor.id().value()).isEqualTo(userId);
                    assertThat(tutor.name()).isEqualTo("Teacher IT");
                    assertThat(tutor.active()).isFalse();
                });
    }

    @Test
    void registerStudent_doesNotCreateTutor() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        RegisterCommand cmd = new RegisterCommand(
                "Student IT", "student-it-" + unique + "@test.com", "secret123", STUDENT);

        AuthResult result = registerUseCase.register(cmd);

        UUID userId = result.userId();
        assertThat(tutorRepository.findById(TutorId.of(userId))).isEmpty();
    }
}
