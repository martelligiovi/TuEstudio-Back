package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.domain.AuthProvider;
import com.tuestudio.auth.domain.Role;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SocialAuthCommandTest {

    @Test
    void registerCommand_fieldsMatchConstruction() {
        var cmd = new SocialAuthCommand(
                "Maria", "maria@google.com", "g-123", AuthProvider.GOOGLE, Optional.of(Role.STUDENT)
        );

        assertThat(cmd.name()).isEqualTo("Maria");
        assertThat(cmd.email()).isEqualTo("maria@google.com");
        assertThat(cmd.providerUserId()).isEqualTo("g-123");
        assertThat(cmd.provider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(cmd.role()).isEqualTo(Optional.of(Role.STUDENT));
    }

    @Test
    void loginCommand_roleIsEmpty() {
        var cmd = new SocialAuthCommand(
                "Maria", "maria@google.com", "g-123", AuthProvider.GOOGLE, Optional.empty()
        );

        assertThat(cmd.role()).isEmpty();
    }
}
