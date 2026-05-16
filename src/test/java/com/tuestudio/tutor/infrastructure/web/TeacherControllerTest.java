package com.tuestudio.tutor.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuestudio.auth.domain.HashedPassword;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import com.tuestudio.tutor.application.port.SubjectLookupPort;
import com.tuestudio.tutor.application.port.SubjectSummary;
import com.tuestudio.tutor.application.usecase.*;
import com.tuestudio.tutor.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeacherController.class)
class TeacherControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean GetTutorProfileUseCase getProfile;
    @MockBean UpdateTutorProfileUseCase updateProfile;
    @MockBean GetTeacherRequestsUseCase getRequests;
    @MockBean AttendRequestUseCase attendRequest;
    @MockBean SubjectLookupPort subjectLookup;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private User teacherUser() {
        return new User(USER_ID, "Ana", "ana@test.com",
                new HashedPassword("$2a$10$hash"), Role.TEACHER);
    }

    private Tutor stubTutor() {
        return new Tutor(TutorId.of(USER_ID), "Ana", null, null, null, null,
                0.0, 0, null, null, false, 0.0,
                List.of(), new Methodology("", List.of()), List.of(), null, List.of(), null);
    }

    /** Creates a Spring Security authentication token with our domain User as principal. */
    private UsernamePasswordAuthenticationToken teacherAuth() {
        return new UsernamePasswordAuthenticationToken(
                teacherUser(), null,
                List.of(new SimpleGrantedAuthority("ROLE_TEACHER")));
    }

    @Test
    void getProfile_returns200_withTutorBody() throws Exception {
        when(getProfile.getById(any())).thenReturn(stubTutor());
        when(subjectLookup.findByIds(anyCollection())).thenReturn(List.of());

        mvc.perform(get("/api/teacher/profile")
                        .with(authentication(teacherAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.missingForActivation").isArray());
    }

    @Test
    void getProfile_returns404_whenTutorNotFound() throws Exception {
        when(getProfile.getById(any())).thenThrow(new TutorNotFoundException(TutorId.of(USER_ID)));

        mvc.perform(get("/api/teacher/profile")
                        .with(authentication(teacherAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void putProfile_returns200_withUpdatedBody() throws Exception {
        Tutor updatedTutor = new Tutor(TutorId.of(USER_ID), "Ana Updated", null, null, null, null,
                0.0, 0, "Mi bio", null, false, 0.0,
                List.of(), new Methodology("", List.of()), List.of(), null, List.of(), null);
        when(updateProfile.update(any())).thenReturn(updatedTutor);
        when(subjectLookup.findByIds(anyCollection())).thenReturn(List.of());

        String body = """
                {
                  "name": "Ana Updated",
                  "bio": "Mi bio",
                  "hourlyRate": 0.0
                }
                """;

        mvc.perform(put("/api/teacher/profile")
                        .with(authentication(teacherAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Updated"));
    }

    @Test
    void putProfile_returns400_whenNameBlank() throws Exception {
        String body = """
                {
                  "name": "",
                  "hourlyRate": 0.0
                }
                """;

        mvc.perform(put("/api/teacher/profile")
                        .with(authentication(teacherAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putProfile_returns404_whenTutorNotFound() throws Exception {
        when(updateProfile.update(any())).thenThrow(new TutorNotFoundException(TutorId.of(USER_ID)));

        String body = """
                {
                  "name": "Ana",
                  "hourlyRate": 0.0
                }
                """;

        mvc.perform(put("/api/teacher/profile")
                        .with(authentication(teacherAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void putProfile_returns422_whenUnknownSubjectId() throws Exception {
        UUID unknownId = UUID.fromString("ffffffff-0000-0000-0000-000000000099");
        when(updateProfile.update(any()))
                .thenThrow(new com.tuestudio.tutor.domain.UnknownSubjectIdsException(Set.of(unknownId)));

        String body = """
                {
                  "name": "Ana",
                  "hourlyRate": 0.0,
                  "subjects": [{"id": "%s"}]
                }
                """.formatted(unknownId);

        mvc.perform(put("/api/teacher/profile")
                        .with(authentication(teacherAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("unknown_subject_ids"))
                .andExpect(jsonPath("$.unknown").isArray());
    }

    @Test
    void putProfile_returnsEnrichedSubjects_whenSubjectsPresent() throws Exception {
        UUID subjectId = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
        Tutor tutorWithSubject = new Tutor(TutorId.of(USER_ID), "Ana", null, null, null, null,
                0.0, 0, "bio", null, false, 1000.0,
                List.of(AssignedSubjectId.of(subjectId)),
                new Methodology("", List.of()),
                List.of(new Schedule("Lun", "9-10")), null, List.of(), null);
        when(updateProfile.update(any())).thenReturn(tutorWithSubject);
        when(subjectLookup.findByIds(anyCollection()))
                .thenReturn(List.of(new SubjectSummary(subjectId, "Álgebra", "math-icon")));

        String body = """
                {
                  "name": "Ana",
                  "bio": "bio",
                  "hourlyRate": 1000.0,
                  "subjects": [{"id": "%s"}]
                }
                """.formatted(subjectId);

        mvc.perform(put("/api/teacher/profile")
                        .with(authentication(teacherAuth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjects[0].id").value(subjectId.toString()))
                .andExpect(jsonPath("$.subjects[0].canonicalName").value("Álgebra"))
                .andExpect(jsonPath("$.subjects[0].icon").value("math-icon"));
    }
}
