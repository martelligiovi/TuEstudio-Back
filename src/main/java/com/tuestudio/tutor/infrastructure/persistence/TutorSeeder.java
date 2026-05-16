package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.auth.application.usecase.PasswordHasher;
import com.tuestudio.auth.domain.HashedPassword;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import com.tuestudio.auth.infrastructure.persistence.UserJpaEntity;
import com.tuestudio.auth.infrastructure.persistence.UserJpaRepository;
import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.tutor.domain.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dev-only seeder. Seeds 4 TEACHER User rows + 4 Tutor rows using deterministic UUIDs.
 * Idempotent: skips if tutors table already has rows.
 *
 * <p>UUIDs are fixed so the dataset is restart-stable (decision per design §6).</p>
 * <p>Canonical subjects must be present before this seeder runs; they are inserted by
 * the V10 Flyway migration ({@code V10__seed_up_subjects.sql}).</p>
 */
@Component
@Profile("!test")
class TutorSeeder {

    // Deterministic, restart-stable UUIDs (design §6)
    private static final UUID TUTOR_1_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID TUTOR_2_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID TUTOR_3_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID TUTOR_4_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

    private final TutorJpaRepository tutorRepository;
    private final ContactRequestJpaRepository contactRepository;
    private final UserJpaRepository userRepository;
    private final SubjectRepositoryPort subjectRepository;
    private final String devPasswordHash;

    TutorSeeder(TutorJpaRepository tutorRepository,
                ContactRequestJpaRepository contactRepository,
                UserJpaRepository userRepository,
                PasswordHasher passwordHasher,
                SubjectRepositoryPort subjectRepository) {
        this.tutorRepository = tutorRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        // Compute once at construction — not per user row (design §9.1)
        this.devPasswordHash = passwordHasher.hash("DevPassword123!");
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    void seed() {
        if (tutorRepository.count() > 0) return;

        // Build a canonical-name → UUID map from the catalog (seeded by subjectCatalogSeeder)
        Map<String, UUID> subjectMap = buildSubjectMap();

        seedUsers();
        seedTutors(subjectMap);
    }

    private Map<String, UUID> buildSubjectMap() {
        Map<String, UUID> rawMap = subjectRepository.findAllCanonicalNameToIdMap();
        if (rawMap.isEmpty()) {
            throw new IllegalStateException(
                    "TutorSeeder: catalog is empty. V10 Flyway migration must run before this seeder.");
        }
        // Normalize keys to lowercase for case-insensitive resolution
        Map<String, UUID> map = new java.util.HashMap<>();
        rawMap.forEach((name, id) -> map.put(name.toLowerCase(), id));
        return map;
    }

    private UUID resolveSubjectId(Map<String, UUID> map, String canonicalName) {
        UUID id = map.get(canonicalName.toLowerCase());
        if (id == null) {
            throw new IllegalStateException(
                    "TutorSeeder: required canonical subject '" + canonicalName
                    + "' not found in catalog. Ensure V10 Flyway migration has run.");
        }
        return id;
    }

    private void seedUsers() {
        userRepository.saveAll(List.of(
                makeUserEntity(TUTOR_1_ID, "María González",  "tutor-seed-1@dev.local"),
                makeUserEntity(TUTOR_2_ID, "Lucas Martínez",  "tutor-seed-2@dev.local"),
                makeUserEntity(TUTOR_3_ID, "Sofía Reyes",     "tutor-seed-3@dev.local"),
                makeUserEntity(TUTOR_4_ID, "Tomás Herrera",   "tutor-seed-4@dev.local")
        ));
    }

    private UserJpaEntity makeUserEntity(UUID id, String name, String email) {
        User user = new User(id, name, email, new HashedPassword(devPasswordHash), Role.TEACHER);
        return UserJpaEntity.fromDomain(user);
    }

    private void seedTutors(Map<String, UUID> subjectMap) {
        tutorRepository.saveAll(List.of(
                TutorJpaEntity.fromDomain(makeTutor(
                        TUTOR_1_ID,
                        "María González",
                        "Matemáticas", "UBA", "Buenos Aires", "Virtual y Presencial",
                        4.9, 127,
                        "Lic. en Matemáticas con 8 años de experiencia docente. Especializada en Análisis y Álgebra para ingeniería y exactas.",
                        "https://randomuser.me/api/portraits/women/44.jpg",
                        2800.0,
                        List.of(
                                AssignedSubjectId.of(resolveSubjectId(subjectMap, "Análisis Matemático I")),
                                AssignedSubjectId.of(resolveSubjectId(subjectMap, "Álgebra Lineal"))),
                        new Methodology("Mi método se basa en la comprensión profunda antes de la memorización.",
                                List.of(new MethodologyFeature("Clases personalizadas", true),
                                        new MethodologyFeature("Material propio", true),
                                        new MethodologyFeature("Seguimiento semanal", true))),
                        List.of(new Schedule("Lunes a Viernes", "9:00 - 20:00"),
                                new Schedule("Sábados", "10:00 - 14:00")),
                        "Consultar disponibilidad con 48hs de anticipación",
                        List.of(new Plan("Clase suelta", "1 clase de 90 min", "$2.800", "por clase", null, false),
                                new Plan("Pack mensual", "8 clases al mes", "$18.000", "por mes", "Popular", true),
                                new Plan("Intensivo", "16 clases al mes", "$32.000", "por mes", null, false)),
                        "+54 11 2345-6789"
                )),
                TutorJpaEntity.fromDomain(makeTutor(
                        TUTOR_2_ID,
                        "Lucas Martínez",
                        "Física", "UTN", "Córdoba", "Virtual",
                        4.7, 89,
                        "Ing. en Electrónica. Doy clases de Física I, II y Electromagnetismo. Metodología basada en problemas reales.",
                        "https://randomuser.me/api/portraits/men/32.jpg",
                        2500.0,
                        List.of(
                                AssignedSubjectId.of(resolveSubjectId(subjectMap, "Física I")),
                                AssignedSubjectId.of(resolveSubjectId(subjectMap, "Física IIb"))),
                        new Methodology("Aprendo mejor resolviendo ejercicios, no leyendo teoría.",
                                List.of(new MethodologyFeature("Ejercicios guiados", true),
                                        new MethodologyFeature("Simulacros de parcial", true),
                                        new MethodologyFeature("Grabación de clases", false))),
                        List.of(new Schedule("Martes y Jueves", "18:00 - 22:00"),
                                new Schedule("Domingos", "10:00 - 16:00")),
                        null,
                        List.of(new Plan("Clase suelta", "1 clase de 90 min", "$2.500", "por clase", null, false),
                                new Plan("Pack 4 clases", "4 clases a coordinar", "$9.000", "por mes", "Recomendado", true)),
                        "+54 351 456-7890"
                )),
                TutorJpaEntity.fromDomain(makeTutor(
                        TUTOR_3_ID,
                        "Sofía Reyes",
                        "Programación", "UNLAM", "Buenos Aires", "Virtual y Presencial",
                        5.0, 43,
                        "Desarrolladora fullstack con 5 años de experiencia. Doy clases de programación desde cero hasta nivel avanzado.",
                        "https://randomuser.me/api/portraits/women/68.jpg",
                        3200.0,
                        List.of(
                                AssignedSubjectId.of(resolveSubjectId(subjectMap, "Introducción a la Programación")),
                                AssignedSubjectId.of(resolveSubjectId(subjectMap, "Base de Datos")),
                                AssignedSubjectId.of(resolveSubjectId(subjectMap, "Estructura de Datos y Algoritmos"))),
                        new Methodology("El código se aprende escribiendo código, no mirando tutoriales.",
                                List.of(new MethodologyFeature("Proyectos reales", true),
                                        new MethodologyFeature("Code review", true),
                                        new MethodologyFeature("Recursos adicionales", true))),
                        List.of(new Schedule("Lunes, Miércoles y Viernes", "19:00 - 23:00")),
                        "Horarios rotativos, consultar por WhatsApp",
                        List.of(new Plan("Clase suelta", "1 clase de 2 hs", "$3.200", "por clase", null, false),
                                new Plan("Mentoría mensual", "8 clases + proyectos", "$22.000", "por mes", "Más elegido", true),
                                new Plan("Bootcamp", "Proyecto completo 1 mes", "$45.000", "único pago", "Intensivo", false)),
                        "+54 11 9876-5432"
                )),
                TutorJpaEntity.fromDomain(makeTutor(
                        TUTOR_4_ID,
                        "Tomás Herrera",
                        "Análisis de Sistemas", "UBA", "Buenos Aires", "Presencial",
                        4.5, 61,
                        "Ing. en Sistemas con experiencia en consultoría y desarrollo de software. Especializado en Análisis y Diseño de Sistemas para carreras de ingeniería informática.",
                        "https://randomuser.me/api/portraits/men/75.jpg",
                        2200.0,
                        List.of(
                                AssignedSubjectId.of(resolveSubjectId(subjectMap, "Análisis de Sistemas")),
                                AssignedSubjectId.of(resolveSubjectId(subjectMap, "Diseño de Sistemas"))),
                        new Methodology("Priorizo el entendimiento del 'por qué' antes de memorizar fórmulas.",
                                List.of(new MethodologyFeature("Material visual", true),
                                        new MethodologyFeature("Resolución en grupo", false),
                                        new MethodologyFeature("Autoevaluaciones", true))),
                        List.of(new Schedule("Miércoles y Viernes", "16:00 - 21:00"),
                                new Schedule("Sábados", "9:00 - 13:00")),
                        null,
                        List.of(new Plan("Clase suelta", "1 clase de 90 min", "$2.200", "por clase", null, false),
                                new Plan("Pack quincenal", "4 clases", "$8.000", "quincenal", "Popular", true)),
                        "+54 11 3344-5566"
                ))
        ));
    }

    private Tutor makeTutor(UUID id, String name, String specialty, String university, String location,
                             String modalidad, double rating, int reviews, String bio, String photoUrl,
                             double hourlyRate, List<AssignedSubjectId> assignedSubjectIds,
                             Methodology methodology,
                             List<Schedule> schedules, String schedulesNote, List<Plan> plans,
                             String phoneNumber) {
        return new Tutor(
                new TutorId(id), name, specialty, university, location, modalidad,
                rating, reviews, bio, photoUrl, true, hourlyRate,
                assignedSubjectIds, methodology, schedules, schedulesNote, plans, phoneNumber
        );
    }
}
