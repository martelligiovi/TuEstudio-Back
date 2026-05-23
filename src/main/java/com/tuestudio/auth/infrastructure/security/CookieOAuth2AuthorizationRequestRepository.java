package com.tuestudio.auth.infrastructure.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * Stateless OAuth2 authorization request repository that stores the request in a cookie.
 * This avoids the need for an HTTP session during the OAuth2 authorization flow,
 * keeping the main security chain STATELESS.
 *
 * Cookie name: oauth2_auth_request
 * Value: Base64-encoded serialized OAuth2AuthorizationRequest
 */
public class CookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    private final boolean secure;
    private final String sameSite;

    public CookieOAuth2AuthorizationRequestRepository() {
        this(false, "Lax");
    }

    public CookieOAuth2AuthorizationRequestRepository(boolean secure, String sameSite) {
        this.secure = secure;
        this.sameSite = sameSite == null || sameSite.isBlank() ? "Lax" : sameSite;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return findCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME)
                .map(Cookie::getValue)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
            return;
        }
        String serialized = serialize(authorizationRequest);
        ResponseCookie cookie = ResponseCookie.from(OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialized)
                .path("/")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(Duration.ofSeconds(COOKIE_EXPIRE_SECONDS))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response) {
        OAuth2AuthorizationRequest existing = loadAuthorizationRequest(request);
        deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        return existing;
    }

    private Optional<Cookie> findCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst();
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        findCookie(request, name).ifPresent(cookie -> {
            ResponseCookie deletion = ResponseCookie.from(name, "")
                    .path("/")
                    .httpOnly(true)
                    .secure(secure)
                    .sameSite(sameSite)
                    .maxAge(Duration.ZERO)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, deletion.toString());
        });
    }

    @SuppressWarnings("deprecation")
    private String serialize(OAuth2AuthorizationRequest request) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(request));
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    private OAuth2AuthorizationRequest deserialize(String value) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(value);
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(decoded);
        } catch (Exception e) {
            return null;
        }
    }
}
