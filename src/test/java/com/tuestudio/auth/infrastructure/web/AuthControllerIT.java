package com.tuestudio.auth.infrastructure.web;

import com.tuestudio.auth.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AuthControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mvc;

    @Test
    void register_returns201_withToken() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana","email":"ana@test.com","password":"secret123","role":"STUDENT"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("ana@test.com"));
    }

    @Test
    void register_returns409_whenEmailDuplicated() throws Exception {
        String body = """
                {"name":"Ana","email":"dup@test.com","password":"secret123","role":"STUDENT"}
                """;
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void login_returns200_afterRegister() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Bob","email":"bob@test.com","password":"pass1234","role":"TEACHER"}
                        """));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bob@test.com","password":"pass1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_returns401_whenWrongPassword() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Bob","email":"bob2@test.com","password":"pass1234","role":"STUDENT"}
                        """));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bob2@test.com","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
