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
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeSubjectIconServiceTest {

    @Mock SubjectRepositoryPort repo;

    ChangeSubjectIconService service;

    @BeforeEach
    void setUp() {
        service = new ChangeSubjectIconService(repo);
    }

    @Test
    void changeIcon_updatesAndSaves() {
        SubjectId id = SubjectId.newId();
        Subject subject = Subject.create(id, "Álgebra");
        when(repo.findById(id)).thenReturn(Optional.of(subject));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Subject result = service.changeIcon(id, "🧮");

        assertThat(result.icon()).isEqualTo("🧮");
        verify(repo).save(subject);
    }

    @Test
    void changeIcon_unknownId_throwsNotFound() {
        SubjectId id = SubjectId.of(UUID.randomUUID());
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeIcon(id, "🧮"))
                .isInstanceOf(SubjectNotFoundException.class);

        verify(repo, never()).save(any());
    }

    @Test
    void changeIcon_withNull_clearsIcon() {
        SubjectId id = SubjectId.newId();
        Subject subject = Subject.create(id, "Física", "🍎");
        when(repo.findById(id)).thenReturn(Optional.of(subject));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Subject result = service.changeIcon(id, null);

        assertThat(result.icon()).isNull();
    }
}
