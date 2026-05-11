package com.tuestudio.auth.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class SocialInitiateControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mvc;

    @Test
    void validRequest_google_student_returns302_withCookieAndLocation() throws Exception {
        MvcResult result = mvc.perform(get("/api/auth/social/initiate")
                        .param("provider", "google")
                        .param("role", "STUDENT"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        assertThat(location).contains("/oauth2/authorization/google");

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("oauth2_role=");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("SameSite=Lax");
    }

    @Test
    void invalidProvider_returns400() throws Exception {
        mvc.perform(get("/api/auth/social/initiate")
                        .param("provider", "invalid_provider")
                        .param("role", "STUDENT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidRole_returns400() throws Exception {
        mvc.perform(get("/api/auth/social/initiate")
                        .param("provider", "google")
                        .param("role", "INVALID_ROLE"))
                .andExpect(status().isBadRequest());
    }
}
