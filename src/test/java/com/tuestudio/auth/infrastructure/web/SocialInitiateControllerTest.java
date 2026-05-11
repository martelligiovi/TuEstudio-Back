package com.tuestudio.auth.infrastructure.web;

import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.infrastructure.security.CookieSigningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SocialInitiateController.class)
@WithMockUser
@org.springframework.test.context.TestPropertySource(properties = "app.base-url=http://localhost:8080")
class SocialInitiateControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    CookieSigningService cookieSigningService;

    @Test
    void validRequest_google_student_returns302_withCookieAndLocation() throws Exception {
        when(cookieSigningService.sign(any(Role.class))).thenReturn("STUDENT.fakehmacsignature");

        MvcResult result = mvc.perform(get("/api/auth/social/initiate")
                        .param("provider", "google")
                        .param("role", "STUDENT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost:8080/oauth2/authorization/google"))
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("oauth2_role=");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("SameSite=Lax");
        assertThat(setCookie).contains("Max-Age=300");
    }

    @Test
    void validRequest_linkedin_teacher_returns302() throws Exception {
        when(cookieSigningService.sign(any(Role.class))).thenReturn("TEACHER.fakehmacsignature");

        mvc.perform(get("/api/auth/social/initiate")
                        .param("provider", "linkedin")
                        .param("role", "TEACHER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost:8080/oauth2/authorization/linkedin"));
    }

    @Test
    void invalidProvider_returns400() throws Exception {
        mvc.perform(get("/api/auth/social/initiate")
                        .param("provider", "facebook")
                        .param("role", "STUDENT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidRole_returns400() throws Exception {
        mvc.perform(get("/api/auth/social/initiate")
                        .param("provider", "google")
                        .param("role", "ADMIN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingRole_returns400() throws Exception {
        mvc.perform(get("/api/auth/social/initiate")
                        .param("provider", "google"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingProvider_returns400() throws Exception {
        mvc.perform(get("/api/auth/social/initiate")
                        .param("role", "STUDENT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginFlow_noRole_returns302ToProvider() throws Exception {
        when(cookieSigningService.signLogin()).thenReturn("LOGIN.fakehmacsignature");

        mvc.perform(get("/api/auth/social/initiate")
                        .param("provider", "google")
                        .param("flow", "login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost:8080/oauth2/authorization/google"));
    }

    @Test
    void registerFlow_missingRole_returns400() throws Exception {
        mvc.perform(get("/api/auth/social/initiate")
                        .param("provider", "google")
                        .param("flow", "register"))
                .andExpect(status().isBadRequest());
    }
}
