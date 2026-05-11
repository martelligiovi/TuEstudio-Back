package com.tuestudio.auth.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthProviderTest {

    @Test
    void authProvider_hasExactlyThreeValues() {
        AuthProvider[] values = AuthProvider.values();
        assertThat(values).hasSize(3);
        assertThat(values).containsExactlyInAnyOrder(
                AuthProvider.LOCAL,
                AuthProvider.GOOGLE,
                AuthProvider.LINKEDIN
        );
    }
}
