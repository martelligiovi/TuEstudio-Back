package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.SubjectLookupPort;
import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTutorProfileServiceTest {

    @Mock TutorRepositoryPort repository;
    @Mock SubjectLookupPort subjectLookup;

    UpdateTutorProfileService service;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final TutorId TUTOR_ID = TutorId.of(USER_ID);
    private static final UUID SUBJECT_A = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID SUBJECT_B = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() {
        service = new UpdateTutorProfileService(repository, subjectLookup);
    }

    private Tutor existingTutor() {
        return new Tutor(TUTOR_ID, "Ana", null, null, null, null,
                4.5, 10, null, null, false, 0.0,
                List.of(), new Methodology("", List.of()), List.of(), null, List.of(), null);
    }

    private UpdateTutorProfileCommand commandWithSubjects(List<UUID> subjectIds) {
        return new UpdateTutorProfileCommand(
                TUTOR_ID, "Ana Updated", "Matemáticas", "UBA", "Buenos Aires", "Presencial",
                "Mi bio completa", "https://photo.com/ana.jpg", 1500.0,
                subjectIds,
                new Methodology("Método activo", List.of(new MethodologyFeature("Pizarrón", true))),
                List.of(new Schedule("Lunes", "10-12")),
                "Clases por Zoom disponibles",
                List.of(new Plan("Básico", "1 clase", "1500", "ARS", null, false)),
                "+5491112345678"
        );
    }

    private UpdateTutorProfileCommand fullCommand() {
        return commandWithSubjects(List.of(SUBJECT_A, SUBJECT_B));
    }

    // --- Happy path ---

    @Test
    void update_happyPath_persistsValidatedSubjectIds() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));
        when(subjectLookup.findExistingIds(Set.of(SUBJECT_A, SUBJECT_B)))
                .thenReturn(Set.of(SUBJECT_A, SUBJECT_B));

        service.update(fullCommand());

        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().assignedSubjectIds())
                .extracting(AssignedSubjectId::value)
                .containsExactlyInAnyOrder(SUBJECT_A, SUBJECT_B);
    }

    @Test
    void update_callsSaveExactlyOnce() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));
        when(subjectLookup.findExistingIds(any())).thenReturn(Set.of(SUBJECT_A, SUBJECT_B));

        service.update(fullCommand());

        verify(repository, times(1)).save(any(Tutor.class));
    }

    @Test
    void update_replacesAllClientEditableFields() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));
        when(subjectLookup.findExistingIds(any())).thenReturn(Set.of(SUBJECT_A, SUBJECT_B));
        UpdateTutorProfileCommand cmd = fullCommand();

        service.update(cmd);

        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);
        verify(repository).save(captor.capture());
        Tutor saved = captor.getValue();

        assertThat(saved.name()).isEqualTo("Ana Updated");
        assertThat(saved.subjectSpecialty()).isEqualTo("Matemáticas");
        assertThat(saved.university()).isEqualTo("UBA");
        assertThat(saved.location()).isEqualTo("Buenos Aires");
        assertThat(saved.modalidad()).isEqualTo("Presencial");
        assertThat(saved.bio()).isEqualTo("Mi bio completa");
        assertThat(saved.photoUrl()).isEqualTo("https://photo.com/ana.jpg");
        assertThat(saved.hourlyRate()).isEqualTo(1500.0);
        assertThat(saved.schedules()).hasSize(1);
        assertThat(saved.schedulesNote()).isEqualTo("Clases por Zoom disponibles");
        assertThat(saved.plans()).hasSize(1);
        assertThat(saved.phoneNumber()).isEqualTo("+5491112345678");
    }

    @Test
    void update_preservesServerManagedFields_ratingAndReviewsCount() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));
        when(subjectLookup.findExistingIds(any())).thenReturn(Set.of(SUBJECT_A, SUBJECT_B));

        service.update(fullCommand());

        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);
        verify(repository).save(captor.capture());
        Tutor saved = captor.getValue();

        assertThat(saved.rating()).isEqualTo(4.5);
        assertThat(saved.reviewsCount()).isEqualTo(10);
    }

    @Test
    void update_returnsSavedTutor() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));
        when(subjectLookup.findExistingIds(any())).thenReturn(Set.of(SUBJECT_A, SUBJECT_B));

        Tutor result = service.update(fullCommand());

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Ana Updated");
    }

    // --- Empty subjects ---

    @Test
    void update_emptySubjects_persistsEmptyList_andTutorRemainsInactive() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));
        when(subjectLookup.findExistingIds(Set.of())).thenReturn(Set.of());

        service.update(commandWithSubjects(List.of()));

        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().assignedSubjectIds()).isEmpty();
        assertThat(captor.getValue().active()).isFalse();
    }

    // --- Unknown UUID ---

    @Test
    void update_unknownUuid_throwsUnknownSubjectIdsException() {
        UUID unknownId = UUID.fromString("ffffffff-0000-0000-0000-000000000099");
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));
        when(subjectLookup.findExistingIds(Set.of(unknownId))).thenReturn(Set.of());

        assertThatThrownBy(() -> service.update(commandWithSubjects(List.of(unknownId))))
                .isInstanceOf(UnknownSubjectIdsException.class)
                .satisfies(ex -> {
                    UnknownSubjectIdsException uex = (UnknownSubjectIdsException) ex;
                    assertThat(uex.unknown()).containsExactly(unknownId);
                });

        verify(repository, never()).save(any());
    }

    @Test
    void update_partiallyUnknownUuids_throwsWithUnknownSubset() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));
        when(subjectLookup.findExistingIds(Set.of(SUBJECT_A, SUBJECT_B)))
                .thenReturn(Set.of(SUBJECT_A)); // SUBJECT_B is unknown

        assertThatThrownBy(() -> service.update(commandWithSubjects(List.of(SUBJECT_A, SUBJECT_B))))
                .isInstanceOf(UnknownSubjectIdsException.class)
                .satisfies(ex -> {
                    UnknownSubjectIdsException uex = (UnknownSubjectIdsException) ex;
                    assertThat(uex.unknown()).containsExactly(SUBJECT_B);
                });

        verify(repository, never()).save(any());
    }

    // --- active flag recomputation ---

    @Test
    void update_recomputesActiveToFalse_whenBioMissing() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));
        when(subjectLookup.findExistingIds(any())).thenReturn(Set.of(SUBJECT_A));

        UpdateTutorProfileCommand cmd = new UpdateTutorProfileCommand(
                TUTOR_ID, "Ana", null, null, null, null,
                null, null, 1500.0,
                List.of(SUBJECT_A),
                new Methodology("", List.of()),
                List.of(new Schedule("Lun", "9-10")),
                null, List.of(), null
        );

        service.update(cmd);

        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().active()).isFalse();
    }

    // --- TutorNotFoundException ---

    @Test
    void update_throwsTutorNotFoundException_whenTutorDoesNotExist() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(fullCommand()))
                .isInstanceOf(TutorNotFoundException.class);

        verify(repository, never()).save(any());
    }
}
