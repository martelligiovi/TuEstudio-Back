package com.tuestudio.auth.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that /api/teacher/** is gated by TEACHER role.
 * - Unauthenticated → 401
 * - STUDENT JWT     → 403
 * - TEACHER JWT     → not 401/403 (200 or 404 depending on data)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class TeacherRoleGateIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

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
    void getProfile_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/teacher/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_studentJwt_returns403() throws Exception {
        String token = registerAndGetToken("student-gate@test.com", "STUDENT");

        mvc.perform(get("/api/teacher/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProfile_teacherJwt_doesNotReturn401Or403() throws Exception {
        String token = registerAndGetToken("teacher-gate@test.com", "TEACHER");

        int status = mvc.perform(get("/api/teacher/profile")
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getStatus();

        // Must not be 401 or 403 — controller processes the request (200 or 404)
        org.assertj.core.api.Assertions.assertThat(status).isNotIn(401, 403);
    }
}
