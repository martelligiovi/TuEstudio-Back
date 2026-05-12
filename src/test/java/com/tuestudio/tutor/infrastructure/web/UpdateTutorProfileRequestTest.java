package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.tutor.application.usecase.UpdateTutorProfileCommand;
import com.tuestudio.tutor.domain.TutorId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UpdateTutorProfileRequestTest {

    private static final UUID ID = UUID.randomUUID();

    @Test
    void toCommand_mapsNameAndId() {
        UpdateTutorProfileRequest req = new UpdateTutorProfileRequest(
                "Ana", null, null, null, null,
                "Mi bio", null, 1000.0,
                null, null, null, null, null, null
        );

        UpdateTutorProfileCommand cmd = req.toCommand(TutorId.of(ID));

        assertThat(cmd.id().value()).isEqualTo(ID);
        assertThat(cmd.name()).isEqualTo("Ana");
        assertThat(cmd.bio()).isEqualTo("Mi bio");
        assertThat(cmd.hourlyRate()).isEqualTo(1000.0);
    }

    @Test
    void toCommand_nullSubjectsBecomesEmptyList() {
        UpdateTutorProfileRequest req = new UpdateTutorProfileRequest(
                "Ana", null, null, null, null,
                null, null, 0.0,
                null, null, null, null, null, null
        );

        UpdateTutorProfileCommand cmd = req.toCommand(TutorId.of(ID));

        assertThat(cmd.subjects()).isEmpty();
        assertThat(cmd.schedules()).isEmpty();
        assertThat(cmd.plans()).isEmpty();
    }

    @Test
    void toCommand_nullMethodologyDefaultsToEmptyMethodology() {
        UpdateTutorProfileRequest req = new UpdateTutorProfileRequest(
                "Ana", null, null, null, null,
                null, null, 0.0,
                null, null, null, null, null, null
        );

        UpdateTutorProfileCommand cmd = req.toCommand(TutorId.of(ID));

        assertThat(cmd.methodology()).isNotNull();
        assertThat(cmd.methodology().intro()).isEqualTo("");
        assertThat(cmd.methodology().features()).isEmpty();
    }

    @Test
    void toCommand_mapsSubjectsCorrectly() {
        var subjects = List.of(new UpdateTutorProfileRequest.SubjectDto("Math", "Algebra", "math-icon"));
        UpdateTutorProfileRequest req = new UpdateTutorProfileRequest(
                "Ana", null, null, null, null,
                null, null, 0.0,
                subjects, null, null, null, null, null
        );

        UpdateTutorProfileCommand cmd = req.toCommand(TutorId.of(ID));

        assertThat(cmd.subjects()).hasSize(1);
        assertThat(cmd.subjects().get(0).name()).isEqualTo("Math");
    }
}
