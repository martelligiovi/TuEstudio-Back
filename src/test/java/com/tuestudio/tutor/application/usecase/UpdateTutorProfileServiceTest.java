package com.tuestudio.tutor.application.usecase;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTutorProfileServiceTest {

    @Mock TutorRepositoryPort repository;

    UpdateTutorProfileService service;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final TutorId TUTOR_ID = TutorId.of(USER_ID);

    @BeforeEach
    void setUp() {
        service = new UpdateTutorProfileService(repository);
    }

    private Tutor existingTutor() {
        return new Tutor(TUTOR_ID, "Ana", null, null, null, null,
                4.5, 10, null, null, false, 0.0,
                List.of(), new Methodology("", List.of()), List.of(), null, List.of(), null);
    }

    private UpdateTutorProfileCommand fullCommand() {
        return new UpdateTutorProfileCommand(
                TUTOR_ID, "Ana Updated", "Matemáticas", "UBA", "Buenos Aires", "Presencial",
                "Mi bio completa", "https://photo.com/ana.jpg", 1500.0,
                List.of(new Subject("Álgebra", "Álgebra lineal", "math")),
                new Methodology("Método activo", List.of(new MethodologyFeature("Pizarrón", true))),
                List.of(new Schedule("Lunes", "10-12")),
                "Clases por Zoom disponibles",
                List.of(new Plan("Básico", "1 clase", "1500", "ARS", null, false)),
                "+5491112345678"
        );
    }

    @Test
    void update_callsSaveExactlyOnce() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));

        service.update(fullCommand());

        verify(repository, times(1)).save(any(Tutor.class));
    }

    @Test
    void update_replacesAllClientEditableFields() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));
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
        assertThat(saved.subjects()).hasSize(1);
        assertThat(saved.schedules()).hasSize(1);
        assertThat(saved.schedulesNote()).isEqualTo("Clases por Zoom disponibles");
        assertThat(saved.plans()).hasSize(1);
        assertThat(saved.phoneNumber()).isEqualTo("+5491112345678");
    }

    @Test
    void update_preservesServerManagedFields_ratingAndReviewsCount() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));

        service.update(fullCommand());

        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);
        verify(repository).save(captor.capture());
        Tutor saved = captor.getValue();

        // rating=4.5, reviewsCount=10 come from existingTutor — not from command
        assertThat(saved.rating()).isEqualTo(4.5);
        assertThat(saved.reviewsCount()).isEqualTo(10);
    }

    @Test
    void update_recomputesActiveToTrue_whenAllConditionsMet() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));

        service.update(fullCommand()); // bio + subjects + schedules + hourlyRate all present

        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().active()).isTrue();
    }

    @Test
    void update_recomputesActiveToFalse_whenBioMissing() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));

        UpdateTutorProfileCommand cmd = new UpdateTutorProfileCommand(
                TUTOR_ID, "Ana", null, null, null, null,
                null, null, 1500.0,
                List.of(new Subject("Math", null, null)),
                new Methodology("", List.of()),
                List.of(new Schedule("Lun", "9-10")),
                null, List.of(), null
        );

        service.update(cmd);

        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().active()).isFalse();
    }

    @Test
    void update_wipesSubjects_whenCommandHasEmptyList() {
        Tutor existing = new Tutor(TUTOR_ID, "Ana", null, null, null, null,
                0.0, 0, "bio", null, false, 100.0,
                List.of(new Subject("Math", null, null)),
                new Methodology("", List.of()), List.of(), null, List.of(), null);
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existing));

        UpdateTutorProfileCommand cmd = new UpdateTutorProfileCommand(
                TUTOR_ID, "Ana", null, null, null, null,
                null, null, 0.0,
                List.of(), // empty subjects — wipes existing
                new Methodology("", List.of()),
                List.of(), null, List.of(), null
        );

        service.update(cmd);

        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().subjects()).isEmpty();
    }

    @Test
    void update_throwsTutorNotFoundException_whenTutorDoesNotExist() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(fullCommand()))
                .isInstanceOf(TutorNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_returnsSavedTutor() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor()));

        Tutor result = service.update(fullCommand());

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Ana Updated");
    }
}
