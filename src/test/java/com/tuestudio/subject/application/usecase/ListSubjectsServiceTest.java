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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListSubjectsServiceTest {

    @Mock SubjectRepositoryPort repo;

    ListSubjectsService service;

    @BeforeEach
    void setUp() {
        service = new ListSubjectsService(repo);
    }

    @Test
    void list_delegatesToRepo() {
        Subject s = Subject.create(SubjectId.newId(), "Álgebra");
        when(repo.findAll()).thenReturn(List.of(s));

        List<Subject> result = service.list();

        assertThat(result).containsExactly(s);
    }
}
