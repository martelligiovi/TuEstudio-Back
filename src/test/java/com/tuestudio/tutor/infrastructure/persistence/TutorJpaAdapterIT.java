package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for TutorJpaAdapter.save() round-trip via real PostgreSQL.
 * Verifies that a saved Tutor can be retrieved with all fields intact.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class TutorJpaAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    TutorRepositoryPort tutorRepository;

    @Test
    void save_andFindById_roundTrip() {
        UUID id = UUID.randomUUID();
        TutorId tutorId = TutorId.of(id);
        Tutor stub = Tutor.stub(tutorId, "Test Tutor");

        tutorRepository.save(stub);

        Optional<Tutor> found = tutorRepository.findById(tutorId);
        assertThat(found).isPresent();
        assertThat(found.get().id().value()).isEqualTo(id);
        assertThat(found.get().name()).isEqualTo("Test Tutor");
        assertThat(found.get().active()).isFalse();
    }

    @Test
    void save_updatesExistingTutor_whenSameIdSaved() {
        UUID id = UUID.randomUUID();
        TutorId tutorId = TutorId.of(id);

        // Save stub first
        tutorRepository.save(Tutor.stub(tutorId, "Original Name"));

        // Save an update over the same id
        Tutor updated = new Tutor(tutorId, "Updated Name", null, "UBA", null, null,
                0.0, 0, "Bio text", null, false, 500.0,
                List.of(new Subject("Math", null, null)),
                new Methodology("Method", List.of()),
                List.of(new Schedule("Mon", "9-10")),
                null, List.of(), null);
        tutorRepository.save(updated);

        Optional<Tutor> found = tutorRepository.findById(tutorId);
        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("Updated Name");
        assertThat(found.get().university()).isEqualTo("UBA");
        assertThat(found.get().hourlyRate()).isEqualTo(500.0);
        assertThat(found.get().subjects()).hasSize(1);
    }

    @Test
    void save_persistsCollections() {
        UUID id = UUID.randomUUID();
        TutorId tutorId = TutorId.of(id);

        Tutor tutor = new Tutor(tutorId, "Ana", null, null, null, null,
                0.0, 0, "Bio", null, false, 1000.0,
                List.of(new Subject("Math", "Algebra", "icon1"), new Subject("Physics", null, null)),
                new Methodology("Active", List.of(new MethodologyFeature("Pizarrón", true))),
                List.of(new Schedule("Mon", "9-10"), new Schedule("Wed", "14-16")),
                "Note", List.of(new Plan("Basic", "1 class", "1000", "ARS", null, false)),
                "+5491199999999");

        tutorRepository.save(tutor);

        Optional<Tutor> found = tutorRepository.findById(tutorId);
        assertThat(found).isPresent();
        Tutor saved = found.get();
        assertThat(saved.subjects()).hasSize(2);
        assertThat(saved.schedules()).hasSize(2);
        assertThat(saved.plans()).hasSize(1);
        assertThat(saved.methodology().intro()).isEqualTo("Active");
        assertThat(saved.methodology().features()).hasSize(1);
    }
}
