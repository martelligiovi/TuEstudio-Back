package com.tuestudio.auth.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuestudio.auth.application.usecase.PasswordHasher;
import com.tuestudio.auth.domain.HashedPassword;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import com.tuestudio.auth.infrastructure.persistence.UserJpaEntity;
import com.tuestudio.auth.infrastructure.persistence.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that /api/admin/** is gated by ADMIN role.
 * Deferred validation — not validated locally due to Testcontainers ↔ Docker Desktop blocker on Windows.
 * Run in CI or WSL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AdminRoleGateIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserJpaRepository userRepository;
    @Autowired PasswordHasher passwordHasher;
    @Autowired JwtTokenAdapter jwtTokenAdapter;

    private String registerAndGetToken(String email, String role) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test User","email":"%s","password":"secret123","role":"%s"}
                                """.formatted(email, role)))
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }

    @Test
    void adminPath_withTeacherJwt_returns403() throws Exception {
        String token = registerAndGetToken("teacher-admin-gate@test.com", "TEACHER");

        mvc.perform(get("/api/admin/subjects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String persistUserAndGetToken(String email, Role role) {
        User user = new User(
                UUID.randomUUID(),
                "Test User",
                email,
                new HashedPassword(passwordHasher.hash("secret123")),
                role);
        userRepository.save(UserJpaEntity.fromDomain(user));
        return jwtTokenAdapter.generate(user);
    }

    @Test
    void adminPath_withAdminJwt_doesNotReturn401Or403() throws Exception {
        String token = persistUserAndGetToken("admin-gate@test.com", Role.ADMIN);

        int status = mvc.perform(get("/api/admin/subjects")
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isNotIn(401, 403);
    }
}
