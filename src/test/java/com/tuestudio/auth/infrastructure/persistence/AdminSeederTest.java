package com.tuestudio.auth.infrastructure.persistence;

import com.tuestudio.auth.application.usecase.PasswordHasher;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.infrastructure.persistence.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock UserJpaRepository userRepository;
    @Mock PasswordHasher passwordHasher;

    AdminSeeder seeder;

    @BeforeEach
    void setUp() {
        when(passwordHasher.hash(any())).thenReturn("hashed-password");
        seeder = new AdminSeeder(userRepository, passwordHasher);
    }

    @Test
    void seed_whenNoAdminExists_createsOne() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);

        seeder.seed();

        verify(userRepository).save(any());
    }

    @Test
    void seed_whenAdminExists_isNoOp() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        seeder.seed();

        verify(userRepository, never()).save(any());
    }

    @Test
    void seededAdmin_hasDeterministicId() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        ArgumentCaptor<UserJpaEntity> captor = ArgumentCaptor.forClass(UserJpaEntity.class);

        seeder.seed();
        seeder.seed();

        verify(userRepository, times(2)).save(captor.capture());
        UUID firstId = captor.getAllValues().get(0).getId();
        UUID secondId = captor.getAllValues().get(1).getId();
        assertThat(firstId).isEqualTo(secondId);
    }
}
