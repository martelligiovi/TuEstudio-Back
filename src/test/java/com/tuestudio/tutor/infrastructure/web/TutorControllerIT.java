package com.tuestudio.tutor.infrastructure.web;

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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class TutorControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired MockMvc mvc;

    @Test
    void search_returnsArray() throws Exception {
        mvc.perform(get("/api/tutors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getById_returns404_whenTutorDoesNotExist() throws Exception {
        mvc.perform(get("/api/tutors/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void contact_returns404_whenTutorDoesNotExist() throws Exception {
        mvc.perform(post("/api/tutors/{id}/contact", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Juan","telefono":"1122334455"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void contact_returns400_whenBodyInvalid() throws Exception {
        mvc.perform(post("/api/tutors/{id}/contact", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
