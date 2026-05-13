package com.tuestudio.subject.infrastructure.persistence;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Dev-only seeder. Seeds the canonical subject catalog ({@code subjects} table) with
 * deterministic UUIDs so the {@code TutorSeeder} can reference them by name.
 * Idempotent: skips if subjects already exist.
 *
 * <p>UUIDs are fixed (restart-stable). TutorSeeder must {@code @DependsOn("subjectCatalogSeeder")}
 * to guarantee ordering.</p>
 */
@Component("subjectCatalogSeeder")
@Profile("!test")
public class SubjectCatalogSeeder {

    // Deterministic, restart-stable UUIDs for canonical subjects
    public static final UUID ANALISIS_MATEMATICO_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    public static final UUID ALGEBRA_LINEAL_ID       = UUID.fromString("10000000-0000-0000-0000-000000000002");
    public static final UUID FISICA_I_ID             = UUID.fromString("10000000-0000-0000-0000-000000000003");
    public static final UUID FISICA_II_ID            = UUID.fromString("10000000-0000-0000-0000-000000000004");
    public static final UUID QUIMICA_GENERAL_ID      = UUID.fromString("10000000-0000-0000-0000-000000000005");
    public static final UUID PROGRAMACION_I_ID       = UUID.fromString("10000000-0000-0000-0000-000000000006");
    public static final UUID BASES_DATOS_ID          = UUID.fromString("10000000-0000-0000-0000-000000000007");
    public static final UUID JAVA_ID                 = UUID.fromString("10000000-0000-0000-0000-000000000008");
    public static final UUID ESTADISTICA_ID          = UUID.fromString("10000000-0000-0000-0000-000000000009");
    public static final UUID TERMODINAMICA_ID        = UUID.fromString("10000000-0000-0000-0000-000000000010");
    public static final UUID QUIMICA_ORGANICA_ID     = UUID.fromString("10000000-0000-0000-0000-000000000011");

    private final SubjectRepositoryPort subjectRepository;

    public SubjectCatalogSeeder(SubjectRepositoryPort subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    void seed() {
        if (!subjectRepository.findAll().isEmpty()) return;

        List.of(
                Subject.create(SubjectId.of(ANALISIS_MATEMATICO_ID), "Análisis Matemático"),
                Subject.create(SubjectId.of(ALGEBRA_LINEAL_ID),       "Álgebra Lineal"),
                Subject.create(SubjectId.of(FISICA_I_ID),             "Física I"),
                Subject.create(SubjectId.of(FISICA_II_ID),            "Física II"),
                Subject.create(SubjectId.of(QUIMICA_GENERAL_ID),      "Química General"),
                Subject.create(SubjectId.of(PROGRAMACION_I_ID),       "Programación I"),
                Subject.create(SubjectId.of(BASES_DATOS_ID),          "Bases de Datos"),
                Subject.create(SubjectId.of(JAVA_ID),                 "Java"),
                Subject.create(SubjectId.of(ESTADISTICA_ID),          "Estadística"),
                Subject.create(SubjectId.of(TERMODINAMICA_ID),        "Termodinámica"),
                Subject.create(SubjectId.of(QUIMICA_ORGANICA_ID),     "Química Orgánica")
        ).forEach(subjectRepository::save);
    }
}
