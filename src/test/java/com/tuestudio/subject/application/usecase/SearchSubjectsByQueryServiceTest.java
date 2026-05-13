package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchSubjectsByQueryServiceTest {

    @Mock SubjectRepositoryPort repo;

    SearchSubjectsByQueryService service;

    @BeforeEach
    void setUp() {
        service = new SearchSubjectsByQueryService(repo);
    }

    @Test
    void search_withBlankQuery_returnsAll() {
        List<Subject> all = List.of(Subject.create(SubjectId.newId(), "Álgebra"));
        when(repo.findAll()).thenReturn(all);

        List<Subject> result = service.search("  ");

        assertThat(result).isEqualTo(all);
        verify(repo, never()).searchByQuery(any());
    }

    @Test
    void search_withNullQuery_returnsAll() {
        List<Subject> all = List.of(Subject.create(SubjectId.newId(), "Álgebra"));
        when(repo.findAll()).thenReturn(all);

        List<Subject> result = service.search(null);

        assertThat(result).isEqualTo(all);
    }

    @Test
    void search_withQuery_delegatesToRepo() {
        List<Subject> matches = List.of(Subject.create(SubjectId.newId(), "Análisis Matemático"));
        when(repo.searchByQuery("mat")).thenReturn(matches);

        List<Subject> result = service.search("mat");

        assertThat(result).isEqualTo(matches);
        verify(repo).searchByQuery("mat");
    }

    @Test
    void search_returnsMatchingByAliasOrName() {
        Subject s = Subject.create(SubjectId.newId(), "Análisis Matemático");
        s.addAlias("Cálculo I");
        when(repo.searchByQuery("cálculo")).thenReturn(List.of(s));

        List<Subject> result = service.search("cálculo");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).canonicalName()).isEqualTo("Análisis Matemático");
    }
}
