package com.tuestudio.auth.infrastructure.security;

import com.tuestudio.auth.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CookieSigningServiceTest {

    private static final String SECRET = "test-signing-secret-min-32-chars-ok";

    private CookieSigningService service;

    @BeforeEach
    void setUp() {
        OAuth2CookieProperties props = new OAuth2CookieProperties(SECRET);
        service = new CookieSigningService(props);
    }

    @Test
    void sign_thenVerify_returnsRole() {
        String cookieValue = service.sign(Role.STUDENT);

        Optional<Role> result = service.verify(cookieValue);

        assertThat(result).isPresent().contains(Role.STUDENT);
    }

    @Test
    void sign_thenVerify_teacherRole() {
        String cookieValue = service.sign(Role.TEACHER);

        Optional<Role> result = service.verify(cookieValue);

        assertThat(result).isPresent().contains(Role.TEACHER);
    }

    @Test
    void verify_tamperedValue_returnsEmpty() {
        String cookieValue = service.sign(Role.STUDENT);
        // tamper the payload
        String tampered = "TEACHER." + cookieValue.split("\\.")[1];

        Optional<Role> result = service.verify(tampered);

        assertThat(result).isEmpty();
    }

    @Test
    void verify_wrongSecret_returnsEmpty() {
        OAuth2CookieProperties wrongProps = new OAuth2CookieProperties("wrong-secret-that-is-long-enough-32chars");
        CookieSigningService other = new CookieSigningService(wrongProps);
        String cookieValue = other.sign(Role.STUDENT);

        Optional<Role> result = service.verify(cookieValue);

        assertThat(result).isEmpty();
    }

    @Test
    void verify_unknownRole_returnsEmpty() {
        // Manually craft a value with unknown role but valid HMAC for that role string
        // The service should reject it because "ADMIN" is not a valid Role
        String validHmac = service.sign(Role.STUDENT).split("\\.")[1];
        String cookieValue = "ADMIN." + validHmac;

        Optional<Role> result = service.verify(cookieValue);

        assertThat(result).isEmpty();
    }

    @Test
    void verify_malformedValue_returnsEmpty() {
        assertThat(service.verify("no-dot-here")).isEmpty();
        assertThat(service.verify("")).isEmpty();
        assertThat(service.verify(null)).isEmpty();
    }

    @Test
    void signLogin_thenIsLoginFlow_returnsTrue() {
        String cookieValue = service.signLogin();
        assertThat(service.isLoginFlow(cookieValue)).isTrue();
    }

    @Test
    void signLogin_verify_returnsEmpty() {
        String cookieValue = service.signLogin();
        assertThat(service.verify(cookieValue)).isEmpty();
    }

    @Test
    void signRole_isLoginFlow_returnsFalse() {
        String cookieValue = service.sign(Role.STUDENT);
        assertThat(service.isLoginFlow(cookieValue)).isFalse();
    }

    @Test
    void isLoginFlow_tamperedLoginSentinel_returnsFalse() {
        String tampered = "LOGIN.invalidsignature";
        assertThat(service.isLoginFlow(tampered)).isFalse();
    }
}
