package com.tuestudio.auth.infrastructure.persistence;

import com.tuestudio.auth.domain.AuthProvider;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserJpaAdapterSocialTest {

    @Mock
    UserJpaRepository jpaRepository;

    UserJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserJpaAdapter(jpaRepository);
    }

    @Test
    void findByProviderAndProviderUserId_delegatesToRepository_whenFound() {
        User social = User.createSocial("Maria", "maria@google.com", AuthProvider.GOOGLE, "g-123", Role.STUDENT);
        UserJpaEntity entity = UserJpaEntity.fromDomain(social);

        when(jpaRepository.findByProviderAndProviderUserId("GOOGLE", "g-123"))
                .thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "g-123");

        assertThat(result).isPresent();
        assertThat(result.get().provider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(result.get().providerUserId()).isEqualTo("g-123");
    }

    @Test
    void findByProviderAndProviderUserId_returnsEmpty_whenNotFound() {
        when(jpaRepository.findByProviderAndProviderUserId("LINKEDIN", "li-999"))
                .thenReturn(Optional.empty());

        Optional<User> result = adapter.findByProviderAndProviderUserId(AuthProvider.LINKEDIN, "li-999");

        assertThat(result).isEmpty();
    }
}
