package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import com.tuestudio.subject.domain.SubjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddSubjectAliasServiceTest {

    @Mock SubjectRepositoryPort repo;

    AddSubjectAliasService service;

    @BeforeEach
    void setUp() {
        service = new AddSubjectAliasService(repo);
    }

    @Test
    void add_validAlias_persists() {
        SubjectId id = SubjectId.newId();
        Subject subject = Subject.create(id, "Matemática");
        when(repo.findById(id)).thenReturn(Optional.of(subject));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Subject result = service.addAlias(id, "Cálculo I");

        assertThat(result.aliases()).contains("Cálculo I");
    }

    @Test
    void add_duplicateIsIdempotent() {
        SubjectId id = SubjectId.newId();
        Subject subject = Subject.create(id, "Matemática");
        subject.addAlias("Cálculo I");
        when(repo.findById(id)).thenReturn(Optional.of(subject));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Subject result = service.addAlias(id, "cálculo i");

        assertThat(result.aliases()).hasSize(1);
    }

    @Test
    void add_unknownSubject_throwsNotFound() {
        SubjectId id = SubjectId.newId();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addAlias(id, "Alias"))
                .isInstanceOf(SubjectNotFoundException.class);
    }

    @Test
    void add_collidingWithCanonical_throwsIllegalArgument() {
        SubjectId id = SubjectId.newId();
        Subject subject = Subject.create(id, "Física");
        when(repo.findById(id)).thenReturn(Optional.of(subject));

        assertThatThrownBy(() -> service.addAlias(id, "física"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
