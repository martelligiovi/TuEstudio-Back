package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.tutor.application.port.SubjectLookupPort;
import com.tuestudio.tutor.application.port.SubjectSummary;
import com.tuestudio.tutor.application.usecase.*;
import com.tuestudio.tutor.domain.TutorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorControllerTest {

    @Mock SearchTutorsUseCase searchTutors;
    @Mock GetTutorUseCase getTutor;
    @Mock RequestContactUseCase requestContact;
    @Mock SubjectLookupPort subjectLookup;

    TutorController controller;

    @BeforeEach
    void setUp() {
        controller = new TutorController(searchTutors, getTutor, requestContact, subjectLookup);
    }

    @Test
    void search_enrichesSubjectsWithCanonicalNameAndIcon() {
        UUID subjectId = UUID.randomUUID();
        TutorSummary summary = new TutorSummary(
                TutorId.of(UUID.randomUUID()), "Ana", "UBA",
                List.of(subjectId), 1500.0, true, null);
        when(searchTutors.search(any())).thenReturn(List.of(summary));
        when(subjectLookup.findByIds(anyCollection()))
                .thenReturn(List.of(new SubjectSummary(subjectId, "Física", "physics-icon")));

        List<TutorSummaryResponse> result = controller.search(null, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subjects()).hasSize(1);
        TutorSummaryResponse.SubjectDto dto = result.get(0).subjects().get(0);
        assertThat(dto.id()).isEqualTo(subjectId);
        assertThat(dto.canonicalName()).isEqualTo("Física");
        assertThat(dto.icon()).isEqualTo("physics-icon");
    }

    @Test
    void search_callsSubjectLookupExactlyOnce_regardlessOfTutorCount() {
        UUID subjectA = UUID.randomUUID();
        UUID subjectB = UUID.randomUUID();
        TutorSummary tutor1 = new TutorSummary(
                TutorId.of(UUID.randomUUID()), "Ana", "UBA",
                List.of(subjectA, subjectB), 1000.0, true, null);
        TutorSummary tutor2 = new TutorSummary(
                TutorId.of(UUID.randomUUID()), "Bob", "UTN",
                List.of(subjectA), 2000.0, true, null);
        when(searchTutors.search(any())).thenReturn(List.of(tutor1, tutor2));
        when(subjectLookup.findByIds(anyCollection())).thenReturn(List.of(
                new SubjectSummary(subjectA, "Álgebra", "algebra-icon"),
                new SubjectSummary(subjectB, "Análisis", "analysis-icon")));

        controller.search(null, null, null, null, null);

        verify(subjectLookup, times(1)).findByIds(anyCollection());
    }

    @Test
    void search_silentlyFiltersOutMissingSubjects_whenCatalogLacksUUID() {
        UUID knownId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        TutorSummary summary = new TutorSummary(
                TutorId.of(UUID.randomUUID()), "Carlos", "UBA",
                List.of(knownId, missingId), 1200.0, true, null);
        when(searchTutors.search(any())).thenReturn(List.of(summary));
        // Catalog only returns the known one — missingId is absent
        when(subjectLookup.findByIds(anyCollection()))
                .thenReturn(List.of(new SubjectSummary(knownId, "Química", "chemistry-icon")));

        List<TutorSummaryResponse> result = controller.search(null, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subjects()).hasSize(1);
        assertThat(result.get(0).subjects().get(0).id()).isEqualTo(knownId);
    }

    @Test
    void search_doesNotCallSubjectLookup_whenNoTutorsFound() {
        when(searchTutors.search(any())).thenReturn(List.of());

        List<TutorSummaryResponse> result = controller.search(null, null, null, null, null);

        assertThat(result).isEmpty();
        verifyNoInteractions(subjectLookup);
    }

    @Test
    void search_doesNotCallSubjectLookup_whenTutorsHaveNoSubjects() {
        TutorSummary summary = new TutorSummary(
                TutorId.of(UUID.randomUUID()), "Diana", "UBA",
                List.of(), 900.0, true, null);
        when(searchTutors.search(any())).thenReturn(List.of(summary));

        List<TutorSummaryResponse> result = controller.search(null, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subjects()).isEmpty();
        verifyNoInteractions(subjectLookup);
    }
}
