package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.auth.application.usecase.PasswordHasher;
import com.tuestudio.auth.infrastructure.persistence.UserJpaRepository;
import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TutorSeederTest {

    @Mock TutorJpaRepository tutorRepository;
    @Mock ContactRequestJpaRepository contactRepository;
    @Mock UserJpaRepository userRepository;
    @Mock PasswordHasher passwordHasher;
    @Mock SubjectRepositoryPort subjectRepository;

    TutorSeeder seeder;

    // Canonical name → UUID mapping for the stub catalog
    private static final UUID ANALISIS_ID    = UUID.randomUUID();
    private static final UUID ALGEBRA_ID     = UUID.randomUUID();
    private static final UUID FISICA_I_ID    = UUID.randomUUID();
    private static final UUID FISICA_II_ID   = UUID.randomUUID();
    private static final UUID QUIMICA_GEN_ID = UUID.randomUUID();
    private static final UUID PROG_I_ID      = UUID.randomUUID();
    private static final UUID BASES_DB_ID    = UUID.randomUUID();
    private static final UUID JAVA_ID        = UUID.randomUUID();
    private static final UUID ESTADISTICA_ID = UUID.randomUUID();
    private static final UUID TERMODI_ID     = UUID.randomUUID();
    private static final UUID QUIMICA_ORG_ID = UUID.randomUUID();

    private static final Map<String, UUID> FULL_CATALOG = Map.ofEntries(
            Map.entry("Análisis Matemático", ANALISIS_ID),
            Map.entry("Álgebra Lineal",      ALGEBRA_ID),
            Map.entry("Física I",            FISICA_I_ID),
            Map.entry("Física II",           FISICA_II_ID),
            Map.entry("Química General",     QUIMICA_GEN_ID),
            Map.entry("Programación I",      PROG_I_ID),
            Map.entry("Bases de Datos",      BASES_DB_ID),
            Map.entry("Java",                JAVA_ID),
            Map.entry("Estadística",         ESTADISTICA_ID),
            Map.entry("Termodinámica",       TERMODI_ID),
            Map.entry("Química Orgánica",    QUIMICA_ORG_ID)
    );

    @BeforeEach
    void setUp() {
        when(passwordHasher.hash(anyString())).thenReturn("$2a$10$hashedpassword");
        when(subjectRepository.findAllCanonicalNameToIdMap()).thenReturn(FULL_CATALOG);
        seeder = new TutorSeeder(tutorRepository, contactRepository, userRepository,
                passwordHasher, subjectRepository);
    }

    @Test
    void seed_isIdempotent_whenTutorsAlreadyExist() {
        when(tutorRepository.count()).thenReturn(4L);
        seeder.seed();
        verify(tutorRepository, never()).saveAll(anyList());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    void seed_insertsUsers_beforeTutors() {
        when(tutorRepository.count()).thenReturn(0L);

        seeder.seed();

        // Users are saved first, then tutors
        var inOrder = inOrder(userRepository, tutorRepository);
        inOrder.verify(userRepository).saveAll(anyList());
        inOrder.verify(tutorRepository).saveAll(anyList());
    }

    @Test
    void seed_usesFixedUuid_forFirstTutor() {
        when(tutorRepository.count()).thenReturn(0L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TutorJpaEntity>> captor = ArgumentCaptor.forClass(List.class);
        seeder.seed();
        verify(tutorRepository).saveAll(captor.capture());

        List<TutorJpaEntity> tutors = captor.getValue();
        UUID expectedFirst = UUID.fromString("00000000-0000-0000-0000-000000000001");
        assertThat(tutors.get(0).getId()).isEqualTo(expectedFirst);
    }

    @Test
    void seederHashesPlaintextPassword_neverPersistsRaw() {
        verify(passwordHasher).hash(argThat(s -> s != null && !s.isBlank() && !s.startsWith("$2a$")));
    }

    @Test
    void seed_throwsIllegalStateException_whenRequiredCanonicalNameMissing() {
        // Return a catalog WITHOUT "Análisis Matemático"
        Map<String, UUID> incompleteMap = Map.ofEntries(
                Map.entry("Álgebra Lineal",   ALGEBRA_ID),
                Map.entry("Física I",         FISICA_I_ID),
                Map.entry("Física II",        FISICA_II_ID),
                Map.entry("Química General",  QUIMICA_GEN_ID),
                Map.entry("Programación I",   PROG_I_ID),
                Map.entry("Bases de Datos",   BASES_DB_ID),
                Map.entry("Java",             JAVA_ID),
                Map.entry("Estadística",      ESTADISTICA_ID),
                Map.entry("Termodinámica",    TERMODI_ID),
                Map.entry("Química Orgánica", QUIMICA_ORG_ID)
        );
        when(subjectRepository.findAllCanonicalNameToIdMap()).thenReturn(incompleteMap);
        seeder = new TutorSeeder(tutorRepository, contactRepository, userRepository,
                passwordHasher, subjectRepository);
        when(tutorRepository.count()).thenReturn(0L);

        assertThatThrownBy(() -> seeder.seed())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Análisis Matemático");
    }

    @Test
    void seed_throwsIllegalStateException_whenCatalogIsEmpty() {
        when(subjectRepository.findAllCanonicalNameToIdMap()).thenReturn(Map.of());
        seeder = new TutorSeeder(tutorRepository, contactRepository, userRepository,
                passwordHasher, subjectRepository);
        when(tutorRepository.count()).thenReturn(0L);

        assertThatThrownBy(() -> seeder.seed())
                .isInstanceOf(IllegalStateException.class);
    }
}
