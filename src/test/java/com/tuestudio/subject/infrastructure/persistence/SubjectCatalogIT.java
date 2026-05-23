package com.tuestudio.subject.infrastructure.persistence;

import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for the subject catalog persistence layer.
 * Deferred validation — not validated locally due to Testcontainers ↔ Docker Desktop blocker on Windows.
 * Run in CI or WSL.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("flyway-test")
@Import(SubjectJpaAdapter.class)
class SubjectCatalogIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired SubjectJpaAdapter adapter;
    @Autowired EntityManager entityManager;

    @Test
    void createCanonical_thenAddAlias_thenSearch_returnsSubject() {
        Subject s = Subject.create(SubjectId.newId(), "Astrobiología Avanzada");
        s.addAlias("ExoplanetologyUniqueToken");
        adapter.save(s);

        List<Subject> results = adapter.searchByQuery("exoplanetologyuniquetoken");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).canonicalName()).isEqualTo("Astrobiología Avanzada");
    }

    @Test
    void searchByAlias_caseInsensitive() {
        Subject s = Subject.create(SubjectId.newId(), "Física Experimental Única");
        s.addAlias("PhysicsUniqueToken");
        adapter.save(s);

        assertThat(adapter.searchByQuery("PHYSICSUNIQUETOKEN")).hasSize(1);
    }

    @Test
    void uniqueCanonicalName_enforcedByMigrationIndex() {
        Subject a = Subject.create(SubjectId.newId(), "MateriaUniqueConstraint");
        Subject b = Subject.create(SubjectId.newId(), "materiauniqueconstraint");
        adapter.save(a);
        entityManager.flush();

        assertThatThrownBy(() -> {
            adapter.save(b);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }
}
