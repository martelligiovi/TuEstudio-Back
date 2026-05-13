package com.tuestudio.subject.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates that V5 migration creates the expected schema.
 * Deferred validation — not validated locally due to Testcontainers ↔ Docker Desktop blocker on Windows.
 * Run in CI or WSL.
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
class SubjectMigrationIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired JdbcTemplate jdbc;

    @Test
    void subjectsTable_exists_withExpectedColumns() {
        List<Map<String, Object>> cols = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns "
                + "WHERE table_name = 'subjects' ORDER BY column_name");

        List<String> names = cols.stream()
                .map(r -> (String) r.get("column_name"))
                .toList();

        assertThat(names).contains("id", "canonical_name");
    }

    @Test
    void subjectAliasesTable_exists_withExpectedColumns() {
        List<Map<String, Object>> cols = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns "
                + "WHERE table_name = 'subject_aliases' ORDER BY column_name");

        List<String> names = cols.stream()
                .map(r -> (String) r.get("column_name"))
                .toList();

        assertThat(names).contains("subject_id", "alias");
    }

    @Test
    void uniqueIndex_onCanonicalNameLower_exists() {
        List<Map<String, Object>> indexes = jdbc.queryForList(
                "SELECT indexname FROM pg_indexes "
                + "WHERE tablename = 'subjects' AND indexname = 'ux_subjects_canonical_name_lower'");

        assertThat(indexes).isNotEmpty();
    }
}
