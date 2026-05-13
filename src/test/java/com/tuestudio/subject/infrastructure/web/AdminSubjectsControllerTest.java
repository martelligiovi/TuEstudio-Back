package com.tuestudio.subject.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuestudio.subject.application.usecase.*;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectAlreadyExistsException;
import com.tuestudio.subject.domain.SubjectId;
import com.tuestudio.subject.domain.SubjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AdminSubjectsController.class, SubjectExceptionHandler.class})
class AdminSubjectsControllerTest {

    @TestConfiguration
    static class MinimalSecurity {
        @Bean
        SecurityFilterChain chain(HttpSecurity http) throws Exception {
            return http
                    .csrf(c -> c.disable())
                    .authorizeHttpRequests(a -> a
                            .requestMatchers("/api/admin/**").hasRole("ADMIN")
                            .anyRequest().authenticated())
                    .build();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CreateSubjectUseCase createSubject;
    @MockBean RenameSubjectUseCase renameSubject;
    @MockBean AddSubjectAliasUseCase addAlias;
    @MockBean RemoveSubjectAliasUseCase removeAlias;
    @MockBean ListSubjectsUseCase listSubjects;

    private Subject subjectWith(String name) {
        return Subject.create(SubjectId.newId(), name);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void post_createsSubject_returns201withDto() throws Exception {
        when(createSubject.create("Álgebra")).thenReturn(subjectWith("Álgebra"));

        mvc.perform(post("/api/admin/subjects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"canonicalName\":\"Álgebra\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.canonicalName").value("Álgebra"))
                .andExpect(jsonPath("$.aliases").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void post_duplicate_returns409() throws Exception {
        when(createSubject.create(anyString())).thenThrow(new SubjectAlreadyExistsException("Álgebra"));

        mvc.perform(post("/api/admin/subjects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"canonicalName\":\"Álgebra\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void post_blank_returns400() throws Exception {
        mvc.perform(post("/api/admin/subjects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"canonicalName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void patch_rename_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(renameSubject.rename(any(), anyString())).thenReturn(subjectWith("Álgebra Lineal"));

        mvc.perform(patch("/api/admin/subjects/" + id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"canonicalName\":\"Álgebra Lineal\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalName").value("Álgebra Lineal"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void patch_unknownId_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(renameSubject.rename(any(), anyString())).thenThrow(new SubjectNotFoundException(SubjectId.of(id)));

        mvc.perform(patch("/api/admin/subjects/" + id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"canonicalName\":\"Nuevo\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postAlias_addsAlias_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        Subject s = subjectWith("Matemática");
        s.addAlias("Cálculo I");
        when(addAlias.addAlias(any(), anyString())).thenReturn(s);

        mvc.perform(post("/api/admin/subjects/" + id + "/aliases")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\":\"Cálculo I\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aliases[0]").value("Cálculo I"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postAlias_collidesWithCanonical_returns400() throws Exception {
        UUID id = UUID.randomUUID();
        when(addAlias.addAlias(any(), anyString()))
                .thenThrow(new IllegalArgumentException("Alias collides with canonical name"));

        mvc.perform(post("/api/admin/subjects/" + id + "/aliases")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alias\":\"matemática\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAlias_removes_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(removeAlias.removeAlias(any(), anyString())).thenReturn(subjectWith("Química"));

        mvc.perform(delete("/api/admin/subjects/" + id + "/aliases/Química General")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void get_listAll_returnsArray() throws Exception {
        when(listSubjects.list()).thenReturn(List.of(subjectWith("Álgebra"), subjectWith("Física")));

        mvc.perform(get("/api/admin/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
