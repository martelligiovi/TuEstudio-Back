package com.tuestudio.subject.infrastructure.persistence;

import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectJpaAdapterSaveTest {

    @Mock CanonicalSubjectJpaRepository repo;

    SubjectJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SubjectJpaAdapter(repo);
    }

    @Test
    void save_newSubject_persistsViaRepo() {
        Subject s = Subject.create(SubjectId.newId(), "Álgebra");
        SubjectJpaEntity entity = SubjectJpaEntity.fromDomain(s);
        when(repo.findById(s.id().value())).thenReturn(Optional.empty());
        when(repo.save(any())).thenReturn(entity);

        adapter.save(s);

        verify(repo).save(any(SubjectJpaEntity.class));
    }

    @Test
    void save_existingSubject_updatesInPlace() {
        Subject s = Subject.create(SubjectId.newId(), "Álgebra");
        SubjectJpaEntity existing = SubjectJpaEntity.fromDomain(s);
        when(repo.findById(s.id().value())).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenReturn(existing);

        adapter.save(s);

        ArgumentCaptor<SubjectJpaEntity> captor = ArgumentCaptor.forClass(SubjectJpaEntity.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existing);
    }

    @Test
    void findById_returnsRehydrated() {
        SubjectId id = SubjectId.newId();
        Subject s = Subject.create(id, "Física");
        SubjectJpaEntity entity = SubjectJpaEntity.fromDomain(s);
        when(repo.findById(id.value())).thenReturn(Optional.of(entity));

        Optional<Subject> result = adapter.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().canonicalName()).isEqualTo("Física");
    }

    @Test
    void save_subjectWithIcon_persistsIcon() {
        Subject s = Subject.create(SubjectId.newId(), "Química", "⚗️");
        SubjectJpaEntity entity = SubjectJpaEntity.fromDomain(s);
        when(repo.findById(s.id().value())).thenReturn(Optional.empty());
        when(repo.save(any())).thenReturn(entity);

        Subject result = adapter.save(s);

        assertThat(result.icon()).isEqualTo("⚗️");
    }

    @Test
    void searchByQuery_blankReturnsAll() {
        when(repo.findAll()).thenReturn(List.of());

        adapter.searchByQuery("  ");

        verify(repo).findAll();
        verify(repo, never()).searchByQuery(any());
    }

    @Test
    void searchByQuery_delegatesToCustomQuery() {
        when(repo.searchByQuery("mat")).thenReturn(List.of());

        adapter.searchByQuery("mat");

        verify(repo).searchByQuery("mat");
    }
}
