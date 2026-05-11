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

@SpringBootTest
@Testcontainers
@ActiveProfiles("flyway-test")
class ContactRequestMigrationIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void v3_creates_contact_requests_table() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'contact_requests'",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void v3_contact_requests_has_all_expected_columns() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_name = 'contact_requests' " +
                "AND column_name IN ('id', 'tutor_id', 'nombre', 'telefono', " +
                "'universidad', 'carrera', 'materia', 'status', 'created_at')",
                Integer.class);
        assertThat(count).isEqualTo(9);
    }

    @Test
    void v3_status_column_is_not_null() {
        String nullable = jdbcTemplate.queryForObject(
                "SELECT is_nullable FROM information_schema.columns " +
                "WHERE table_name = 'contact_requests' AND column_name = 'status'",
                String.class);
        assertThat(nullable).isEqualToIgnoringCase("NO");
    }

    @Test
    void v3_created_at_column_is_not_null() {
        String nullable = jdbcTemplate.queryForObject(
                "SELECT is_nullable FROM information_schema.columns " +
                "WHERE table_name = 'contact_requests' AND column_name = 'created_at'",
                String.class);
        assertThat(nullable).isEqualToIgnoringCase("NO");
    }
}
