package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.application.port.UserRepositoryPort;
import com.tuestudio.auth.domain.AuthProvider;
import com.tuestudio.auth.domain.HashedPassword;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryPortContractTest {

    @Mock
    UserRepositoryPort repository;

    @Test
    void findByProviderAndProviderUserId_returnsUser_whenPresent() {
        User user = User.createSocial("Maria", "maria@google.com", AuthProvider.GOOGLE, "g-123", Role.STUDENT);
        when(repository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "g-123"))
                .thenReturn(Optional.of(user));

        Optional<User> result = repository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "g-123");

        assertThat(result).isPresent();
        assertThat(result.get().providerUserId()).isEqualTo("g-123");
    }

    @Test
    void findByProviderAndProviderUserId_returnsEmpty_whenAbsent() {
        when(repository.findByProviderAndProviderUserId(AuthProvider.LINKEDIN, "li-999"))
                .thenReturn(Optional.empty());

        Optional<User> result = repository.findByProviderAndProviderUserId(AuthProvider.LINKEDIN, "li-999");

        assertThat(result).isEmpty();
    }
}
