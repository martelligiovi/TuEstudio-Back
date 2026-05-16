package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.tutor.application.port.SubjectSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
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

        when(subjectRepositoryPort.findExistingIds(Set.of(existingId, missingId)))
                .thenReturn(Set.of(existingId));

        Set<UUID> result = adapter.findExistingIds(Set.of(existingId, missingId));

        assertThat(result).containsExactly(existingId);
    }

    @Test
    void findExistingIds_returnsEmptySet_whenNoneExist() {
        UUID id = UUID.randomUUID();
        when(subjectRepositoryPort.findExistingIds(Set.of(id))).thenReturn(Set.of());

        assertThat(adapter.findExistingIds(Set.of(id))).isEmpty();
    }

    // ---- findByIds ----

    @Test
    void findByIds_mapsTwoSubjectsToSummaries() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(subjectRepositoryPort.findCanonicalNamesByIds(Set.of(id1, id2)))
                .thenReturn(Map.of(id1, "Math", id2, "Physics"));

        List<SubjectSummary> result = adapter.findByIds(List.of(id1, id2));

        assertThat(result).extracting(SubjectSummary::canonicalName)
                .containsExactlyInAnyOrder("Math", "Physics");
    }

    @Test
    void findByIds_omitsUnknownIds() {
        UUID knownId = UUID.randomUUID();
        UUID unknownId = UUID.randomUUID();

        when(subjectRepositoryPort.findCanonicalNamesByIds(anyCollection()))
                .thenReturn(Map.of(knownId, "Chemistry"));

        List<SubjectSummary> result = adapter.findByIds(List.of(knownId, unknownId));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(knownId);
    }

    @Test
    void findByIds_returnsEmptyList_whenAllUnknown() {
        when(subjectRepositoryPort.findCanonicalNamesByIds(anyCollection())).thenReturn(Map.of());

        assertThat(adapter.findByIds(List.of(UUID.randomUUID()))).isEmpty();
    }

    // ---- findIdsMatching ----

    @Test
    void findIdsMatching_delegatesToFindIdsMatching_andReturnsThem() {
        UUID id = UUID.randomUUID();

        when(subjectRepositoryPort.findIdsMatching("física")).thenReturn(List.of(id));

        Set<UUID> result = adapter.findIdsMatching("física");

        assertThat(result).containsExactly(id);
        verify(subjectRepositoryPort, times(1)).findIdsMatching("física");
    }

    @Test
    void findIdsMatching_returnsEmptySet_whenNoMatch() {
        when(subjectRepositoryPort.findIdsMatching(anyString())).thenReturn(List.of());

        assertThat(adapter.findIdsMatching("nonexistent")).isEmpty();
    }
}
