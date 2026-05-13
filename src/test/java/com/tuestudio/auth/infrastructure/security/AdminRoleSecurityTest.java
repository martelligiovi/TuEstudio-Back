package com.tuestudio.auth.infrastructure.security;

import com.tuestudio.subject.application.usecase.*;
import com.tuestudio.subject.infrastructure.web.AdminSubjectsController;
import com.tuestudio.subject.infrastructure.web.SubjectsController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AdminSubjectsController.class, SubjectsController.class})
class AdminRoleSecurityTest {

    @TestConfiguration
    static class MinimalSecurity {
        @Bean
        SecurityFilterChain chain(HttpSecurity http) throws Exception {
            return http
                    .csrf(c -> c.disable())
                    .authorizeHttpRequests(a -> a
                            .requestMatchers("/api/admin/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/subjects/**").permitAll()
                            .anyRequest().authenticated())
                    .build();
        }
    }

    @Autowired MockMvc mvc;

    @MockBean CreateSubjectUseCase createSubject;
    @MockBean RenameSubjectUseCase renameSubject;
    @MockBean AddSubjectAliasUseCase addAlias;
    @MockBean RemoveSubjectAliasUseCase removeAlias;
    @MockBean ListSubjectsUseCase listSubjects;
    @MockBean SearchSubjectsByQueryUseCase searchSubjects;

    @Test
    @WithMockUser(roles = "TEACHER")
    void adminPath_withTeacherRole_returns403() throws Exception {
        mvc.perform(get("/api/admin/subjects"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void adminPath_withStudentRole_returns403() throws Exception {
        mvc.perform(get("/api/admin/subjects"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPath_withAdminRole_returns200or404() throws Exception {
        when(listSubjects.list()).thenReturn(List.of());

        int status = mvc.perform(get("/api/admin/subjects"))
                .andReturn().getResponse().getStatus();

        org.assertj.core.api.Assertions.assertThat(status).isNotIn(401, 403);
    }

    @Test
    void publicSubjectsPath_anonymous_returns200() throws Exception {
        when(searchSubjects.search(null)).thenReturn(List.of());

        mvc.perform(get("/api/subjects"))
                .andExpect(status().isOk());
    }
}
