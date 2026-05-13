package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import com.tuestudio.tutor.application.port.SubjectSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectLookupAdapterTest {

    @Mock SubjectRepositoryPort subjectRepositoryPort;

    SubjectLookupAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SubjectLookupAdapter(subjectRepositoryPort);
    }

    // ---- findExistingIds ----

    @Test
    void findExistingIds_returnsOnlyPresentUuids() {
        UUID existingId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();

        when(subjectRepositoryPort.findById(new SubjectId(existingId)))
                .thenReturn(Optional.of(Subject.create(SubjectId.of(existingId), "Math")));
        when(subjectRepositoryPort.findById(new SubjectId(missingId)))
                .thenReturn(Optional.empty());

        Set<UUID> result = adapter.findExistingIds(Set.of(existingId, missingId));

        assertThat(result).containsExactly(existingId);
    }

    @Test
    void findExistingIds_returnsEmptySet_whenNoneExist() {
        UUID id = UUID.randomUUID();
        when(subjectRepositoryPort.findById(any())).thenReturn(Optional.empty());

        assertThat(adapter.findExistingIds(Set.of(id))).isEmpty();
    }

    // ---- findByIds ----

    @Test
    void findByIds_mapsTwoSubjectsToSummaries() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Subject s1 = Subject.create(SubjectId.of(id1), "Math");
        Subject s2 = Subject.create(SubjectId.of(id2), "Physics");

        when(subjectRepositoryPort.findById(new SubjectId(id1))).thenReturn(Optional.of(s1));
        when(subjectRepositoryPort.findById(new SubjectId(id2))).thenReturn(Optional.of(s2));

        List<SubjectSummary> result = adapter.findByIds(List.of(id1, id2));

        assertThat(result).extracting(SubjectSummary::canonicalName)
                .containsExactlyInAnyOrder("Math", "Physics");
    }

    @Test
    void findByIds_omitsUnknownIds() {
        UUID knownId = UUID.randomUUID();
        UUID unknownId = UUID.randomUUID();
        Subject s = Subject.create(SubjectId.of(knownId), "Chemistry");

        when(subjectRepositoryPort.findById(new SubjectId(knownId))).thenReturn(Optional.of(s));
        when(subjectRepositoryPort.findById(new SubjectId(unknownId))).thenReturn(Optional.empty());

        List<SubjectSummary> result = adapter.findByIds(List.of(knownId, unknownId));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(knownId);
    }

    @Test
    void findByIds_returnsEmptyList_whenAllUnknown() {
        when(subjectRepositoryPort.findById(any())).thenReturn(Optional.empty());

        assertThat(adapter.findByIds(List.of(UUID.randomUUID()))).isEmpty();
    }

    // ---- findIdsMatching ----

    @Test
    void findIdsMatching_delegatesToSearchByQuery_andExtractsIds() {
        UUID id = UUID.randomUUID();
        Subject s = Subject.create(SubjectId.of(id), "Física I");

        when(subjectRepositoryPort.searchByQuery("física")).thenReturn(List.of(s));

        Set<UUID> result = adapter.findIdsMatching("física");

        assertThat(result).containsExactly(id);
        verify(subjectRepositoryPort, times(1)).searchByQuery("física");
    }

    @Test
    void findIdsMatching_returnsEmptySet_whenNoMatch() {
        when(subjectRepositoryPort.searchByQuery(anyString())).thenReturn(List.of());

        assertThat(adapter.findIdsMatching("nonexistent")).isEmpty();
    }
}
