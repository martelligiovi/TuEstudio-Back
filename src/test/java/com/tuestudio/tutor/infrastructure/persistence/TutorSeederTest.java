package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.auth.application.usecase.PasswordHasher;
import com.tuestudio.auth.infrastructure.persistence.UserJpaEntity;
import com.tuestudio.auth.infrastructure.persistence.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorSeederTest {

    @Mock TutorJpaRepository tutorRepository;
    @Mock ContactRequestJpaRepository contactRepository;
    @Mock UserJpaRepository userRepository;
    @Mock PasswordHasher passwordHasher;

    TutorSeeder seeder;

    @BeforeEach
    void setUp() {
        when(passwordHasher.hash(anyString())).thenReturn("$2a$10$hashedpassword");
        seeder = new TutorSeeder(tutorRepository, contactRepository, userRepository, passwordHasher);
    }

    @Test
    void seed_isIdempotent_whenTutorsAlreadyExist() {
        when(tutorRepository.count()).thenReturn(4L);
        seeder.seed();
        verify(tutorRepository, never()).saveAll(anyList());
        verify(userRepository, never()).saveAll(anyList());
    }

    @Test
    void seed_insertsUsers_beforeTutors() {
        when(tutorRepository.count()).thenReturn(0L);
        InOrder order = inOrder(userRepository, tutorRepository);

        seeder.seed();

        order.verify(userRepository).saveAll(anyList());
        order.verify(tutorRepository).saveAll(anyList());
    }

    @Test
    void seed_usesFixedUuid_forFirstTutor() {
        when(tutorRepository.count()).thenReturn(0L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TutorJpaEntity>> captor = ArgumentCaptor.forClass(List.class);
        seeder.seed();
        verify(tutorRepository).saveAll(captor.capture());

        List<TutorJpaEntity> tutors = captor.getValue();
        UUID expectedFirst = UUID.fromString("00000000-0000-0000-0000-000000000001");
        assertThat(tutors.get(0).getId()).isEqualTo(expectedFirst);
    }

    @Test
    void seed_passwordIsNotPlaintext() {
        when(tutorRepository.count()).thenReturn(0L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserJpaEntity>> captor = ArgumentCaptor.forClass(List.class);
        seeder.seed();
        verify(userRepository).saveAll(captor.capture());

        List<UserJpaEntity> users = captor.getValue();
        assertThat(users).allSatisfy(u ->
                assertThat(u.getHashedPassword()).startsWith("$2a$")
        );
    }
}
