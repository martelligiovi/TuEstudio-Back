package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.domain.Methodology;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorJpaAdapterSaveTest {

    @Mock
    TutorJpaRepository repository;

    TutorJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TutorJpaAdapter(repository);
    }

    @Test
    void save_delegatesToJpaRepositorySave() {
        Tutor tutor = stubTutor(UUID.randomUUID(), "Ana");
        adapter.save(tutor);
        verify(repository).save(any(TutorJpaEntity.class));
    }

    @Test
    void save_passesEntityWithMatchingId() {
        UUID id = UUID.randomUUID();
        Tutor tutor = stubTutor(id, "Ana");

        ArgumentCaptor<TutorJpaEntity> captor = ArgumentCaptor.forClass(TutorJpaEntity.class);
        adapter.save(tutor);
        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(id);
    }

    @Test
    void save_passesEntityWithMatchingName() {
        UUID id = UUID.randomUUID();
        Tutor tutor = stubTutor(id, "Carlos");

        ArgumentCaptor<TutorJpaEntity> captor = ArgumentCaptor.forClass(TutorJpaEntity.class);
        adapter.save(tutor);
        verify(repository).save(captor.capture());

        // TutorJpaEntity is package-private; we rely on toDomain round-trip to verify name
        // Since getId() is package-private but accessible within same package, we use toDomain
        Tutor roundTripped = captor.getValue().toDomain();
        assertThat(roundTripped.name()).isEqualTo("Carlos");
    }

    private Tutor stubTutor(UUID id, String name) {
        return new Tutor(
                TutorId.of(id), name, null, null, null, null,
                0.0, 0, null, null, false, 0.0,
                List.of(), new Methodology("", List.of()), List.of(),
                null, List.of(), null
        );
    }
}
