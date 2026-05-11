package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.application.port.TokenPort;
import com.tuestudio.auth.application.port.TutorProvisioningPort;
import com.tuestudio.auth.application.port.UserRepositoryPort;
import com.tuestudio.auth.domain.AuthProvider;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialAuthServiceTest {

    @Mock UserRepositoryPort userRepository;
    @Mock TokenPort tokenPort;
    @Mock TutorProvisioningPort tutorProvisioning;

    SocialAuthService service;

    @BeforeEach
    void setUp() {
        service = new SocialAuthService(userRepository, tokenPort, tutorProvisioning);
    }

    @Test
    void authenticate_newUser_savesAndReturnsToken() {
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "g-123"))
                .thenReturn(Optional.empty());
        when(tokenPort.generate(any())).thenReturn("jwt-token");

        var cmd = new SocialAuthCommand("Maria", "maria@google.com", "g-123", AuthProvider.GOOGLE, Optional.of(Role.TEACHER));
        AuthResult result = service.authenticate(cmd);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.provider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(saved.providerUserId()).isEqualTo("g-123");
        assertThat(saved.role()).isEqualTo(Role.TEACHER);
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo("maria@google.com");
        assertThat(result.role()).isEqualTo(Role.TEACHER);
    }

    @Test
    void authenticate_existingUser_doesNotSave_andPreservesRole() {
        User existing = User.createSocial("Maria", "maria@google.com", AuthProvider.GOOGLE, "g-123", Role.STUDENT);
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "g-123"))
                .thenReturn(Optional.of(existing));
        when(tokenPort.generate(existing)).thenReturn("jwt-token");

        // Command has role=TEACHER but existing user has STUDENT — role must be preserved
        var cmd = new SocialAuthCommand("Maria", "maria@google.com", "g-123", AuthProvider.GOOGLE, Optional.of(Role.TEACHER));
        AuthResult result = service.authenticate(cmd);

        verify(userRepository, never()).save(any());
        assertThat(result.role()).isEqualTo(Role.STUDENT);
        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void authenticate_sameEmailDifferentProvider_createsNewUser() {
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.LINKEDIN, "li-456"))
                .thenReturn(Optional.empty());
        when(tokenPort.generate(any())).thenReturn("jwt-token");

        var cmd = new SocialAuthCommand("Maria", "maria@google.com", "li-456", AuthProvider.LINKEDIN, Optional.of(Role.STUDENT));
        service.authenticate(cmd);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().provider()).isEqualTo(AuthProvider.LINKEDIN);
    }

    @Test
    void authenticate_loginFlow_existingUser_returnsToken() {
        User existing = User.createSocial("Maria", "maria@google.com", AuthProvider.GOOGLE, "g-123", Role.STUDENT);
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "g-123"))
                .thenReturn(Optional.of(existing));
        when(tokenPort.generate(existing)).thenReturn("jwt-token");

        var cmd = new SocialAuthCommand("Maria", "maria@google.com", "g-123", AuthProvider.GOOGLE, Optional.empty());
        AuthResult result = service.authenticate(cmd);

        verify(userRepository, never()).save(any());
        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    void authenticate_loginFlow_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "g-123"))
                .thenReturn(Optional.empty());

        var cmd = new SocialAuthCommand("Maria", "maria@google.com", "g-123", AuthProvider.GOOGLE, Optional.empty());

        assertThatThrownBy(() -> service.authenticate(cmd))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Task 3.2 — TEACHER provisioning on social auth
    // -------------------------------------------------------------------------

    @Test
    void authenticate_newTeacher_callsProvisioningPortOnce() {
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "g-123"))
                .thenReturn(Optional.empty());
        when(tokenPort.generate(any())).thenReturn("jwt");

        var cmd = new SocialAuthCommand("Maria", "maria@google.com", "g-123", AuthProvider.GOOGLE, Optional.of(Role.TEACHER));
        service.authenticate(cmd);

        verify(tutorProvisioning, times(1)).provisionFor(any(User.class));
    }

    @Test
    void authenticate_newTeacher_provisionsWithCorrectUser() {
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "g-123"))
                .thenReturn(Optional.empty());
        when(tokenPort.generate(any())).thenReturn("jwt");

        var cmd = new SocialAuthCommand("Maria", "maria@google.com", "g-123", AuthProvider.GOOGLE, Optional.of(Role.TEACHER));
        service.authenticate(cmd);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(tutorProvisioning).provisionFor(captor.capture());
        assertThat(captor.getValue().role()).isEqualTo(Role.TEACHER);
    }

    @Test
    void authenticate_newStudent_doesNotCallProvisioningPort() {
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "g-456"))
                .thenReturn(Optional.empty());
        when(tokenPort.generate(any())).thenReturn("jwt");

        var cmd = new SocialAuthCommand("Pedro", "pedro@google.com", "g-456", AuthProvider.GOOGLE, Optional.of(Role.STUDENT));
        service.authenticate(cmd);

        verify(tutorProvisioning, never()).provisionFor(any());
    }

    @Test
    void authenticate_existingTeacher_doesNotReProvision() {
        User existing = User.createSocial("Maria", "maria@google.com", AuthProvider.GOOGLE, "g-123", Role.TEACHER);
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "g-123"))
                .thenReturn(Optional.of(existing));
        when(tokenPort.generate(existing)).thenReturn("jwt");

        var cmd = new SocialAuthCommand("Maria", "maria@google.com", "g-123", AuthProvider.GOOGLE, Optional.of(Role.TEACHER));
        service.authenticate(cmd);

        verify(tutorProvisioning, never()).provisionFor(any());
    }

    @Test
    void authenticate_provisioningFailure_propagatesAndRollsBack() {
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "g-123"))
                .thenReturn(Optional.empty());
        doThrow(new RuntimeException("provisioning failed")).when(tutorProvisioning).provisionFor(any());

        var cmd = new SocialAuthCommand("Maria", "maria@google.com", "g-123", AuthProvider.GOOGLE, Optional.of(Role.TEACHER));
        assertThatThrownBy(() -> service.authenticate(cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("provisioning failed");
    }
}
