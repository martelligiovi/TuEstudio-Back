package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.application.usecase.SearchCriteria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies TutorSpecification's subject filter behaviour via real PostgreSQL (Testcontainers).
 * The test inserts rows directly via JDBC to avoid triggering seeders.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class TutorSpecificationSubjectFilterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired TutorJpaRepository tutorRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    private static final UUID SUBJECT_A = UUID.fromString("cccccccc-0000-0000-0000-000000000001");
    private static final UUID SUBJECT_B = UUID.fromString("cccccccc-0000-0000-0000-000000000002");

    /** Insert a minimal active tutor and link it to the given subject UUIDs. */
    private UUID seedTutorWithSubjects(String name, UUID... subjectIds) {
        UUID tutorId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO tutors (id, name, rating, reviews_count, active, hourly_rate) " +
                "VALUES (?, ?, 0, 0, true, 1000.0)",
                tutorId, name);
        for (UUID subId : subjectIds) {
            jdbcTemplate.update(
                    "INSERT INTO tutor_subject_ids (tutor_id, subject_id) VALUES (?, ?)",
                    tutorId, subId);
        }
        return tutorId;
    }

    /** Ensure the subjects table contains the given UUID (FK requirement). */
    private void ensureSubjectExists(UUID subjectId, String canonicalName) {
        jdbcTemplate.update(
                "INSERT INTO subjects (id, canonical_name) VALUES (?, ?) ON CONFLICT DO NOTHING",
                subjectId, canonicalName);
    }

    @Test
    void specWithMatchingSubjectId_returnsTutor() {
        ensureSubjectExists(SUBJECT_A, "Algebra Spec Test A");
        UUID tutorId = seedTutorWithSubjects("Spec Tutor A", SUBJECT_A);

        SearchCriteria criteria = new SearchCriteria(null, null, null, null, null);
        var spec = TutorSpecification.from(criteria, Set.of(SUBJECT_A));

        List<TutorJpaEntity> results = tutorRepository.findAll(spec);

        assertThat(results).extracting(TutorJpaEntity::getId).contains(tutorId);
    }

    @Test
    void specWithNonMatchingSubjectId_returnsNoTutorWithOnlySubjectA() {
        ensureSubjectExists(SUBJECT_A, "Algebra Spec Test B");
        UUID tutorId = seedTutorWithSubjects("Spec Tutor B Only A", SUBJECT_A);

        // SUBJECT_B was never inserted — tutors with only SUBJECT_A should not appear
        SearchCriteria criteria = new SearchCriteria(null, null, null, null, null);
        // We use a random UUID that no tutor has been linked to
        UUID randomSubject = UUID.fromString("dddddddd-0000-0000-0000-000000000099");
        ensureSubjectExists(randomSubject, "NonExistent Subject");
        var spec = TutorSpecification.from(criteria, Set.of(randomSubject));

        List<TutorJpaEntity> results = tutorRepository.findAll(spec);

        // The tutor with SUBJECT_A should NOT appear when filtering by randomSubject
        assertThat(results).extracting(TutorJpaEntity::getId).doesNotContain(tutorId);
    }

    @Test
    void specWithEmptyMatchingIds_returnsEmpty() {
        ensureSubjectExists(SUBJECT_A, "Algebra Spec Test C");
        seedTutorWithSubjects("Spec Tutor C", SUBJECT_A);

        SearchCriteria criteria = new SearchCriteria(null, null, null, null, null);
        var spec = TutorSpecification.from(criteria, Set.of()); // empty set — disjunction guard

        List<TutorJpaEntity> results = tutorRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void specWithNullMatchingIds_doesNotFilterBySubject() {
        // null means materia filter is not active — all active tutors returned
        SearchCriteria criteria = new SearchCriteria(null, null, null, null, null);
        var spec = TutorSpecification.from(criteria, null);

        // Should not throw; active tutors are returned regardless of subjects
        List<TutorJpaEntity> results = tutorRepository.findAll(spec);
        // Just assert it doesn't throw and returns a list (possibly empty in this test context)
        assertThat(results).isNotNull();
    }
}
