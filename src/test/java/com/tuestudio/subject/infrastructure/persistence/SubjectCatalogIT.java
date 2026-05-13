package com.tuestudio.subject.infrastructure.persistence;

import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@Testcontainers
@ActiveProfiles("test")
@Import(SubjectJpaAdapter.class)
class SubjectCatalogIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired SubjectJpaAdapter adapter;

    @Test
    void createCanonical_thenAddAlias_thenSearch_returnsSubject() {
        Subject s = Subject.create(SubjectId.newId(), "Análisis Matemático");
        s.addAlias("Cálculo I");
        adapter.save(s);

        List<Subject> results = adapter.searchByQuery("cálculo");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).canonicalName()).isEqualTo("Análisis Matemático");
    }

    @Test
    void searchByAlias_caseInsensitive() {
        Subject s = Subject.create(SubjectId.newId(), "Física");
        s.addAlias("Physics");
        adapter.save(s);

        assertThat(adapter.searchByQuery("PHYSICS")).hasSize(1);
    }

    @Test
    void uniqueCanonicalName_enforcedByMigrationIndex() {
        Subject a = Subject.create(SubjectId.newId(), "Química");
        Subject b = Subject.create(SubjectId.newId(), "química");
        adapter.save(a);

        assertThatThrownBy(() -> adapter.save(b))
                .isInstanceOf(Exception.class);
    }
}
