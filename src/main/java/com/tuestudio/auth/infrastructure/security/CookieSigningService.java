package com.tuestudio.auth.infrastructure.security;

import com.tuestudio.auth.domain.Role;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;

public class CookieSigningService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SEPARATOR = ".";
    private static final String LOGIN_SENTINEL = "LOGIN";

    private final byte[] secretBytes;

    public CookieSigningService(OAuth2CookieProperties properties) {
        this.secretBytes = properties.signingSecret().getBytes(StandardCharsets.UTF_8);
    }

    public String sign(Role role) {
        return signPayload(role.name());
    }

    public String signLogin() {
        return signPayload(LOGIN_SENTINEL);
    }

    /** Returns the Role if the cookie is a register flow, empty otherwise. */
    public Optional<Role> verify(String cookieValue) {
        return verifyPayload(cookieValue)
                .filter(p -> !LOGIN_SENTINEL.equals(p))
                .flatMap(p -> {
                    try {
                        return Optional.of(Role.valueOf(p));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                });
    }

    /** Returns true if the cookie is a valid login-flow sentinel. */
    public boolean isLoginFlow(String cookieValue) {
        return verifyPayload(cookieValue)
                .map(LOGIN_SENTINEL::equals)
                .orElse(false);
    }

    private String signPayload(String payload) {
        String hmac = computeHmac(payload);
        return payload + SEPARATOR + hmac;
    }

    private Optional<String> verifyPayload(String cookieValue) {
        if (cookieValue == null || cookieValue.isBlank()) {
            return Optional.empty();
        }
        int dotIndex = cookieValue.indexOf(SEPARATOR);
        if (dotIndex < 0) {
            return Optional.empty();
        }
        String payload = cookieValue.substring(0, dotIndex);
        String providedHmac = cookieValue.substring(dotIndex + 1);
        String expectedHmac = computeHmac(payload);
        return constantTimeEquals(expectedHmac, providedHmac) ? Optional.of(payload) : Optional.empty();
    }

    private String computeHmac(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretBytes, HMAC_ALGORITHM));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC", e);
        }
    }

    /**
     * Constant-time comparison to avoid timing attacks.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
