package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.application.port.TokenPort;
import com.tuestudio.auth.application.port.UserRepositoryPort;
import com.tuestudio.auth.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock UserRepositoryPort userRepository;
    @Mock TokenPort tokenPort;
    @Mock PasswordHasher passwordHasher;

    RegisterService service;

    @BeforeEach
    void setUp() {
        service = new RegisterService(userRepository, tokenPort, passwordHasher);
    }

    @Test
    void register_returnsTokenAndUserData() {
        when(userRepository.existsByEmail("ana@mail.com")).thenReturn(false);
        when(passwordHasher.hash("secret")).thenReturn("hashed");
        when(tokenPort.generate(any())).thenReturn("jwt-token");

        var cmd = new RegisterCommand("Ana", "ana@mail.com", "secret", Role.STUDENT);
        var result = service.register(cmd);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo("ana@mail.com");
        assertThat(result.role()).isEqualTo(Role.STUDENT);
        verify(userRepository).save(any());
    }

    @Test
    void register_throwsDuplicateEmail_whenEmailExists() {
        when(userRepository.existsByEmail("ana@mail.com")).thenReturn(true);

        var cmd = new RegisterCommand("Ana", "ana@mail.com", "secret", Role.STUDENT);
        assertThatThrownBy(() -> service.register(cmd))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any());
    }
}
