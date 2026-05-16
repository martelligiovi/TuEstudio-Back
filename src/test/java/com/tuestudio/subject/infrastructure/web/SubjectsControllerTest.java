package com.tuestudio.subject.infrastructure.web;

import com.tuestudio.subject.application.usecase.SearchSubjectsByQueryUseCase;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubjectsController.class)
class SubjectsControllerTest {

    @TestConfiguration
    static class MinimalSecurity {
        @Bean
        SecurityFilterChain chain(HttpSecurity http) throws Exception {
            return http
                    .csrf(c -> c.disable())
                    .authorizeHttpRequests(a -> a
                            .requestMatchers(HttpMethod.GET, "/api/subjects/**").permitAll()
                            .anyRequest().authenticated())
                    .build();
        }
    }

    @Autowired MockMvc mvc;
    @MockBean SearchSubjectsByQueryUseCase searchSubjects;

    @Test
    void getSubjects_blankQuery_returnsAll() throws Exception {
        List<Subject> subjects = List.of(
                Subject.create(SubjectId.newId(), "Álgebra"),
                Subject.create(SubjectId.newId(), "Física")
        );
        when(searchSubjects.search(null)).thenReturn(subjects);

        mvc.perform(get("/api/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getSubjects_withQuery_returnsMatching() throws Exception {
        List<Subject> subjects = List.of(Subject.create(SubjectId.newId(), "Álgebra Lineal"));
        when(searchSubjects.search("álgebra")).thenReturn(subjects);

        mvc.perform(get("/api/subjects").param("q", "álgebra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Álgebra Lineal"));
    }

    @Test
    void getSubjects_returnsIdNameAndIcon() throws Exception {
        Subject s = Subject.create(SubjectId.newId(), "Química");
        s.addAlias("Química General");
        when(searchSubjects.search(null)).thenReturn(List.of(s));

        mvc.perform(get("/api/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].aliases").doesNotExist());
    }

    @Test
    void getSubjects_withIcon_includesIconInResponse() throws Exception {
        Subject s = Subject.create(SubjectId.newId(), "Álgebra", "🧮");
        when(searchSubjects.search(null)).thenReturn(List.of(s));

        mvc.perform(get("/api/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].icon").value("🧮"));
    }

    @Test
    void getSubjects_anonymous_returns200() throws Exception {
        when(searchSubjects.search(null)).thenReturn(List.of());

        mvc.perform(get("/api/subjects"))
                .andExpect(status().isOk());
    }
}
