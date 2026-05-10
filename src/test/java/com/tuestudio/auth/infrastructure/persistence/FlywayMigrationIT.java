package com.tuestudio.auth.infrastructure.persistence;

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

@SpringBootTest
@Testcontainers
@ActiveProfiles("flyway-test")
class FlywayMigrationIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void v1_creates_users_table() {
        // Table exists (if not, query would throw)
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'users'",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void v2_makes_hashed_password_nullable() {
        String nullable = jdbcTemplate.queryForObject(
                "SELECT is_nullable FROM information_schema.columns " +
                "WHERE table_name = 'users' AND column_name = 'hashed_password'",
                String.class);
        assertThat(nullable).isEqualToIgnoringCase("YES");
    }

    @Test
    void v2_adds_provider_column_with_default_local() {
        // Column exists
        String columnDefault = jdbcTemplate.queryForObject(
                "SELECT column_default FROM information_schema.columns " +
                "WHERE table_name = 'users' AND column_name = 'provider'",
                String.class);
        assertThat(columnDefault).isNotNull();
        assertThat(columnDefault.toLowerCase()).contains("local");
    }

    @Test
    void v2_adds_provider_user_id_column_nullable() {
        String nullable = jdbcTemplate.queryForObject(
                "SELECT is_nullable FROM information_schema.columns " +
                "WHERE table_name = 'users' AND column_name = 'provider_user_id'",
                String.class);
        assertThat(nullable).isEqualToIgnoringCase("YES");
    }

    @Test
    void v2_creates_unique_index_on_provider_and_provider_user_id() {
        Integer indexCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes " +
                "WHERE tablename = 'users' AND indexname = 'idx_users_provider_pid'",
                Integer.class);
        assertThat(indexCount).isEqualTo(1);
    }

    @Test
    void unique_constraint_rejects_duplicate_provider_and_provider_user_id() {
        // Insert a row with provider + provider_user_id
        jdbcTemplate.execute(
                "INSERT INTO users (id, name, email, role, provider, provider_user_id) " +
                "VALUES (gen_random_uuid(), 'Test User', 'migration_test@example.com', " +
                "'STUDENT', 'GOOGLE', 'unique-test-sub-migration')");

        // Attempt to insert duplicate — should fail with constraint violation
        assertThatThrownBy(() ->
                jdbcTemplate.execute(
                        "INSERT INTO users (id, name, email, role, provider, provider_user_id) " +
                        "VALUES (gen_random_uuid(), 'Test User 2', 'migration_test2@example.com', " +
                        "'STUDENT', 'GOOGLE', 'unique-test-sub-migration')"))
                .hasMessageContaining("idx_users_provider_pid");
    }
}
