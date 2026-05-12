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

/**
 * Verifies that V4 Flyway migration creates all 5 tutor tables
 * via information_schema queries on a clean Testcontainers PostgreSQL instance.
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

    @Test
    void v4_creates_tutors_table() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'tutors'",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void v4_creates_tutor_subjects_table() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'tutor_subjects'",
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
        for (String table : new String[]{"tutor_subjects", "tutor_schedules", "tutor_plans", "tutor_methodology_features"}) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_name = '" + table + "' AND column_name = 'tutor_id'",
                    Integer.class);
            assertThat(count).as("Table %s must have tutor_id", table).isEqualTo(1);
        }
    }
}
