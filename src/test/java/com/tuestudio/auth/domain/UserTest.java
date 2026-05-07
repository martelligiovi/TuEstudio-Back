package com.tuestudio.auth.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void createSocial_setsProviderAndNullPassword() {
        User user = User.createSocial("Maria", "maria@google.com", AuthProvider.GOOGLE, "g-123", Role.STUDENT);

        assertThat(user.provider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(user.providerUserId()).isEqualTo("g-123");
        assertThat(user.password()).isNull();
        assertThat(user.isSocial()).isTrue();
    }

    @Test
    void createSocial_optionalPassword_isEmpty() {
        User user = User.createSocial("Maria", "maria@google.com", AuthProvider.GOOGLE, "g-123", Role.STUDENT);

        assertThat(user.optionalPassword()).isEmpty();
    }

    @Test
    void localUser_isNotSocial() {
        User user = User.create("Ana", "ana@mail.com", new HashedPassword("hashed"), Role.STUDENT);

        assertThat(user.isSocial()).isFalse();
        assertThat(user.provider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(user.optionalPassword()).isPresent();
    }

    @Test
    void hashedPassword_rejectsNull() {
        assertThatThrownBy(() -> new HashedPassword(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void socialUser_password_isNull_withoutThrowing() {
        User user = User.createSocial("Maria", "maria@google.com", AuthProvider.GOOGLE, "g-123", Role.STUDENT);
        // Accessing password() should not throw — it returns null for social users
        assertThat(user.password()).isNull();
    }
}
