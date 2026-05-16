package com.tuestudio.subject.infrastructure.persistence;

import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectJpaAdapterCrossContextTest {

    @Mock
    CanonicalSubjectJpaRepository repo;

    SubjectJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SubjectJpaAdapter(repo);
    }

    // ---- findExistingIds ----

    @Test
    void findExistingIds_emptyInput_returnsEmptySet() {
        Set<UUID> result = adapter.findExistingIds(Set.of());
        assertThat(result).isEmpty();
        verifyNoInteractions(repo);
    }

    @Test
    void findExistingIds_knownIds_returnsFoundIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID unknownId = UUID.randomUUID();

        Subject s1 = Subject.create(SubjectId.of(id1), "Álgebra");
        Subject s2 = Subject.create(SubjectId.of(id2), "Física");
        SubjectJpaEntity e1 = SubjectJpaEntity.fromDomain(s1);
        SubjectJpaEntity e2 = SubjectJpaEntity.fromDomain(s2);

        when(repo.findAllById(any())).thenReturn(List.of(e1, e2));

        Set<UUID> result = adapter.findExistingIds(Set.of(id1, id2, unknownId));

        assertThat(result).containsExactlyInAnyOrder(id1, id2);
        assertThat(result).doesNotContain(unknownId);
    }

    @Test
    void findExistingIds_allUnknown_returnsEmptySet() {
        UUID unknownId = UUID.randomUUID();
        when(repo.findAllById(any())).thenReturn(List.of());

        Set<UUID> result = adapter.findExistingIds(Set.of(unknownId));

        assertThat(result).isEmpty();
    }

    // ---- findCanonicalNamesByIds ----

    @Test
    void findCanonicalNamesByIds_emptyInput_returnsEmptyMap() {
        Map<UUID, String> result = adapter.findCanonicalNamesByIds(List.of());
        assertThat(result).isEmpty();
        verifyNoInteractions(repo);
    }

    @Test
    void findCanonicalNamesByIds_knownIds_returnsMapping() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Subject s1 = Subject.create(SubjectId.of(id1), "Matemática");
        Subject s2 = Subject.create(SubjectId.of(id2), "Biología");
        SubjectJpaEntity e1 = SubjectJpaEntity.fromDomain(s1);
        SubjectJpaEntity e2 = SubjectJpaEntity.fromDomain(s2);

        when(repo.findAllById(any())).thenReturn(List.of(e1, e2));

        Map<UUID, String> result = adapter.findCanonicalNamesByIds(List.of(id1, id2));

        assertThat(result).containsEntry(id1, "Matemática");
        assertThat(result).containsEntry(id2, "Biología");
    }

    @Test
    void findCanonicalNamesByIds_unknownIdsAbsent() {
        UUID knownId = UUID.randomUUID();
        UUID unknownId = UUID.randomUUID();

        Subject s = Subject.create(SubjectId.of(knownId), "Historia");
        SubjectJpaEntity e = SubjectJpaEntity.fromDomain(s);

        when(repo.findAllById(any())).thenReturn(List.of(e));

        Map<UUID, String> result = adapter.findCanonicalNamesByIds(List.of(knownId, unknownId));

        assertThat(result).containsOnlyKeys(knownId);
        assertThat(result).doesNotContainKey(unknownId);
    }

    // ---- findIdsMatching ----

    @Test
    void findIdsMatching_blankQuery_returnsAllIds() {
        UUID id1 = UUID.randomUUID();
        Subject s1 = Subject.create(SubjectId.of(id1), "Química");
        SubjectJpaEntity e1 = SubjectJpaEntity.fromDomain(s1);
        when(repo.findAll()).thenReturn(List.of(e1));

        List<UUID> result = adapter.findIdsMatching("  ");

        assertThat(result).containsExactly(id1);
        verify(repo).findAll();
        verify(repo, never()).searchByQuery(anyString());
    }

    @Test
    void findIdsMatching_queryWithResults_returnsMatchingIds() {
        UUID id1 = UUID.randomUUID();
        Subject s1 = Subject.create(SubjectId.of(id1), "Química Orgánica");
        SubjectJpaEntity e1 = SubjectJpaEntity.fromDomain(s1);
        when(repo.searchByQuery("quim")).thenReturn(List.of(e1));

        List<UUID> result = adapter.findIdsMatching("quim");

        assertThat(result).containsExactly(id1);
        verify(repo).searchByQuery("quim");
    }

    @Test
    void findIdsMatching_noMatch_returnsEmptyList() {
        when(repo.searchByQuery("xyz")).thenReturn(List.of());

        List<UUID> result = adapter.findIdsMatching("xyz");

        assertThat(result).isEmpty();
    }

    // ---- findAllCanonicalNameToIdMap ----

    @Test
    void findAllCanonicalNameToIdMap_emptyRepo_returnsEmptyMap() {
        when(repo.findAll()).thenReturn(List.of());

        Map<String, UUID> result = adapter.findAllCanonicalNameToIdMap();

        assertThat(result).isEmpty();
    }

    @Test
    void findAllCanonicalNameToIdMap_returnsNameToIdMapping() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Subject s1 = Subject.create(SubjectId.of(id1), "Lengua");
        Subject s2 = Subject.create(SubjectId.of(id2), "Geografía");
        SubjectJpaEntity e1 = SubjectJpaEntity.fromDomain(s1);
        SubjectJpaEntity e2 = SubjectJpaEntity.fromDomain(s2);
        when(repo.findAll()).thenReturn(List.of(e1, e2));

        Map<String, UUID> result = adapter.findAllCanonicalNameToIdMap();

        assertThat(result).containsEntry("Lengua", id1);
        assertThat(result).containsEntry("Geografía", id2);
    }
}
