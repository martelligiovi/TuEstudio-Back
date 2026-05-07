package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.application.port.TokenPort;
import com.tuestudio.auth.application.port.UserRepositoryPort;
import com.tuestudio.auth.domain.AuthProvider;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceSocialGuardTest {

    @Mock UserRepositoryPort userRepository;
    @Mock TokenPort tokenPort;
    @Mock PasswordHasher passwordHasher;

    LoginService service;

    @BeforeEach
    void setUp() {
        service = new LoginService(userRepository, tokenPort, passwordHasher);
    }

    @Test
    void login_throws_whenUserIsSocial() {
        User socialUser = User.createSocial("Maria", "maria@google.com", AuthProvider.GOOGLE, "g-123", Role.STUDENT);
        when(userRepository.findByEmail("maria@google.com")).thenReturn(Optional.of(socialUser));

        assertThatThrownBy(() -> service.login(new LoginCommand("maria@google.com", "anything")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(tokenPort, never()).generate(any());
    }
}
