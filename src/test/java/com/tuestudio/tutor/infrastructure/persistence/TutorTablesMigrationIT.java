package com.tuestudio.tutor.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies Flyway migrations create the expected tutor schema.
 * V4 creates the base tables; V7 drops tutor_subjects and creates tutor_subject_ids.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("flyway-test")
class TutorTablesMigrationIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    JdbcTemplate jdbcTemplate;

    // ---- V4 table checks ----

    @Test
    void v4_creates_tutors_table() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'tutors'",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void v4_creates_tutor_schedules_table() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'tutor_schedules'",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void v4_creates_tutor_plans_table() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'tutor_plans'",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void v4_creates_tutor_methodology_features_table() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'tutor_methodology_features'",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void v4_tutors_table_has_all_required_columns() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_name = 'tutors' " +
                "AND column_name IN ('id', 'name', 'subject_specialty', 'university', 'location', " +
                "'modalidad', 'rating', 'reviews_count', 'bio', 'photo_url', 'active', " +
                "'hourly_rate', 'schedules_note', 'phone_number', 'methodology_intro')",
                Integer.class);
        assertThat(count).isEqualTo(15);
    }

    @Test
    void v4_tutors_name_is_not_null() {
        String nullable = jdbcTemplate.queryForObject(
                "SELECT is_nullable FROM information_schema.columns " +
                "WHERE table_name = 'tutors' AND column_name = 'name'",
                String.class);
        assertThat(nullable).isEqualToIgnoringCase("NO");
    }

    @Test
    void v4_collection_tables_have_tutor_id_column() {
        for (String table : new String[]{"tutor_schedules", "tutor_plans", "tutor_methodology_features"}) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_name = '" + table + "' AND column_name = 'tutor_id'",
                    Integer.class);
            assertThat(count).as("Table %s must have tutor_id", table).isEqualTo(1);
        }
    }

    // ---- V7 table checks (T-010) ----

    @Test
    void v7_creates_tutor_subject_ids_table() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'tutor_subject_ids'",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void v7_tutor_subject_ids_has_tutor_id_and_subject_id_columns() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_name = 'tutor_subject_ids' " +
                "AND column_name IN ('tutor_id', 'subject_id')",
                Integer.class);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void v7_tutor_subject_ids_has_composite_pk_on_tutor_and_subject() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints tc " +
                "JOIN information_schema.constraint_column_usage ccu " +
                "  ON tc.constraint_name = ccu.constraint_name " +
                "WHERE tc.table_name = 'tutor_subject_ids' " +
                "  AND tc.constraint_type = 'PRIMARY KEY' " +
                "  AND ccu.column_name IN ('tutor_id', 'subject_id')",
                Integer.class);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void v7_tutor_subjects_table_no_longer_exists() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'tutor_subjects'",
                Integer.class);
        assertThat(count).isEqualTo(0);
    }

    @Test
    void v7_duplicate_insert_on_same_tutor_subject_pair_throws_pk_violation() {
        // Insert a tutor first (no FK to users in this test — just raw JDBC)
        jdbcTemplate.execute(
                "INSERT INTO tutors (id, name, rating, reviews_count, active, hourly_rate) " +
                "VALUES ('aaaaaaaa-0000-0000-0000-000000000001', 'Test', 0, 0, false, 0)");

        // Insert a subject first (FK on tutor_subject_ids.subject_id -> subjects.id)
        jdbcTemplate.execute(
                "INSERT INTO subjects (id, canonical_name) " +
                "VALUES ('bbbbbbbb-0000-0000-0000-000000000001', 'TestSubject')");

        jdbcTemplate.execute(
                "INSERT INTO tutor_subject_ids (tutor_id, subject_id) " +
                "VALUES ('aaaaaaaa-0000-0000-0000-000000000001', 'bbbbbbbb-0000-0000-0000-000000000001')");

        assertThatThrownBy(() ->
                jdbcTemplate.execute(
                        "INSERT INTO tutor_subject_ids (tutor_id, subject_id) " +
                        "VALUES ('aaaaaaaa-0000-0000-0000-000000000001', 'bbbbbbbb-0000-0000-0000-000000000001')")
        ).isInstanceOf(Exception.class);
    }
}
