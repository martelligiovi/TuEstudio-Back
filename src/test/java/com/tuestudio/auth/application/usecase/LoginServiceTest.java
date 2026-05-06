package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.application.port.TokenPort;
import com.tuestudio.auth.application.port.UserRepositoryPort;
import com.tuestudio.auth.domain.HashedPassword;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock UserRepositoryPort userRepository;
    @Mock TokenPort tokenPort;
    @Mock PasswordHasher passwordHasher;

    LoginService service;

    @BeforeEach
    void setUp() {
        service = new LoginService(userRepository, tokenPort, passwordHasher);
    }

    @Test
    void login_returnsToken_whenCredentialsValid() {
        var user = User.create("Ana", "ana@mail.com", new HashedPassword("hashed"), Role.STUDENT);
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("secret", "hashed")).thenReturn(true);
        when(tokenPort.generate(user)).thenReturn("jwt-token");

        var result = service.login(new LoginCommand("ana@mail.com", "secret"));

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo("ana@mail.com");
    }

    @Test
    void login_throws_whenEmailNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginCommand("x@mail.com", "secret")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throws_whenPasswordWrong() {
        var user = User.create("Ana", "ana@mail.com", new HashedPassword("hashed"), Role.STUDENT);
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginCommand("ana@mail.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
