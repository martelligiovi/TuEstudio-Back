package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.application.port.TokenPort;
import com.tuestudio.auth.application.port.TutorProvisioningPort;
import com.tuestudio.auth.application.port.UserRepositoryPort;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock UserRepositoryPort userRepository;
    @Mock TokenPort tokenPort;
    @Mock PasswordHasher passwordHasher;
    @Mock TutorProvisioningPort tutorProvisioning;

    RegisterService service;

    @BeforeEach
    void setUp() {
        service = new RegisterService(userRepository, tokenPort, passwordHasher, tutorProvisioning);
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

    @Test
    void register_rejectsAdminRole() {
        var cmd = new RegisterCommand("Admin", "admin@mail.com", "secret", Role.ADMIN);

        assertThatThrownBy(() -> service.register(cmd))
                .isInstanceOf(InvalidRegistrationRoleException.class);

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(tutorProvisioning, never()).provisionFor(any());
    }

    // -------------------------------------------------------------------------
    // Task 3.1 — TEACHER provisioning
    // -------------------------------------------------------------------------

    @Test
    void register_teacher_callsProvisioningPortOnce() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordHasher.hash(anyString())).thenReturn("hashed");
        when(tokenPort.generate(any())).thenReturn("jwt");

        var cmd = new RegisterCommand("Lucas", "lucas@mail.com", "secret", Role.TEACHER);
        service.register(cmd);

        verify(tutorProvisioning, times(1)).provisionFor(any(User.class));
    }

    @Test
    void register_teacher_provisionsWithCorrectUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordHasher.hash(anyString())).thenReturn("hashed");
        when(tokenPort.generate(any())).thenReturn("jwt");

        var cmd = new RegisterCommand("Lucas", "lucas@mail.com", "secret", Role.TEACHER);
        service.register(cmd);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(tutorProvisioning).provisionFor(captor.capture());
        assertThat(captor.getValue().role()).isEqualTo(Role.TEACHER);
    }

    @Test
    void register_student_doesNotCallProvisioningPort() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordHasher.hash(anyString())).thenReturn("hashed");
        when(tokenPort.generate(any())).thenReturn("jwt");

        var cmd = new RegisterCommand("Ana", "ana@mail.com", "secret", Role.STUDENT);
        service.register(cmd);

        verify(tutorProvisioning, never()).provisionFor(any());
    }

    @Test
    void register_isAnnotatedTransactional() throws NoSuchMethodException {
        Method method = RegisterService.class.getMethod("register", RegisterCommand.class);
        boolean hasMethodAnnotation = method.isAnnotationPresent(Transactional.class);
        boolean hasClassAnnotation = RegisterService.class.isAnnotationPresent(Transactional.class);
        assertThat(hasMethodAnnotation || hasClassAnnotation)
                .as("RegisterService.register must be @Transactional at method or class level")
                .isTrue();
    }

    @Test
    void register_provisioningFailure_propagatesException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordHasher.hash(anyString())).thenReturn("hashed");
        doThrow(new RuntimeException("provisioning failed")).when(tutorProvisioning).provisionFor(any());

        var cmd = new RegisterCommand("Lucas", "lucas@mail.com", "secret", Role.TEACHER);
        assertThatThrownBy(() -> service.register(cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("provisioning failed");
    }
}
