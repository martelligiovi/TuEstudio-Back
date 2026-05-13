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
class RemoveSubjectAliasServiceTest {

    @Mock SubjectRepositoryPort repo;

    RemoveSubjectAliasService service;

    @BeforeEach
    void setUp() {
        service = new RemoveSubjectAliasService(repo);
    }

    @Test
    void remove_existing_succeeds() {
        SubjectId id = SubjectId.newId();
        Subject subject = Subject.create(id, "Química");
        subject.addAlias("Química General");
        when(repo.findById(id)).thenReturn(Optional.of(subject));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Subject result = service.removeAlias(id, "Química General");

        assertThat(result.aliases()).isEmpty();
    }

    @Test
    void remove_missing_noop() {
        SubjectId id = SubjectId.newId();
        Subject subject = Subject.create(id, "Química");
        when(repo.findById(id)).thenReturn(Optional.of(subject));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> service.removeAlias(id, "no existe")).doesNotThrowAnyException();
    }

    @Test
    void remove_unknownSubject_throwsNotFound() {
        SubjectId id = SubjectId.newId();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeAlias(id, "algo"))
                .isInstanceOf(SubjectNotFoundException.class);
    }
}
