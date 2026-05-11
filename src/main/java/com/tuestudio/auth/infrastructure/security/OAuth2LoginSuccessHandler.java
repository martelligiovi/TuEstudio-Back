package com.tuestudio.auth.infrastructure.security;

import com.tuestudio.auth.application.usecase.AuthResult;
import com.tuestudio.auth.application.usecase.SocialAuthCommand;
import com.tuestudio.auth.application.usecase.SocialAuthUseCase;
import com.tuestudio.auth.application.usecase.UserNotFoundException;
import com.tuestudio.auth.domain.AuthProvider;
import com.tuestudio.auth.domain.Role;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String ROLE_COOKIE_NAME = "oauth2_role";

    private final SocialAuthUseCase socialAuthUseCase;
    private final CookieSigningService cookieSigningService;
    private final String frontendRedirectUrl;

    public OAuth2LoginSuccessHandler(
            SocialAuthUseCase socialAuthUseCase,
            CookieSigningService cookieSigningService,
            String frontendRedirectUrl) {
        this.socialAuthUseCase = socialAuthUseCase;
        this.cookieSigningService = cookieSigningService;
        this.frontendRedirectUrl = frontendRedirectUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User principal = token.getPrincipal();
        String registrationId = token.getAuthorizedClientRegistrationId();

        String rawCookie = readRawCookie(request);
        boolean loginFlow = rawCookie != null && cookieSigningService.isLoginFlow(rawCookie);
        Optional<Role> roleOptional = rawCookie != null ? cookieSigningService.verify(rawCookie) : Optional.empty();

        clearRoleCookie(response);

        if (!loginFlow && roleOptional.isEmpty()) {
            response.sendRedirect(errorUrl("invalid_role"));
            return;
        }

        String email = principal.getAttribute("email");
        if (email == null || email.isBlank()) {
            response.sendRedirect(errorUrl("missing_email"));
            return;
        }

        String name = extractName(principal, registrationId);
        String providerUserId = principal.getName();
        AuthProvider provider = mapProvider(registrationId);

        SocialAuthCommand command = new SocialAuthCommand(name, email, providerUserId, provider, roleOptional);

        try {
            AuthResult result = socialAuthUseCase.authenticate(command);
            invalidateSession(request);
            response.sendRedirect(frontendRedirectUrl + "?token=" + result.token());
        } catch (UserNotFoundException e) {
            invalidateSession(request);
            response.sendRedirect(errorUrl("account_not_found"));
        }
    }

    private String errorUrl(String reason) {
        String base = frontendRedirectUrl.endsWith("/callback")
                ? frontendRedirectUrl.substring(0, frontendRedirectUrl.length() - "/callback".length())
                : frontendRedirectUrl;
        return base + "/error?reason=" + reason;
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private String readRawCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> ROLE_COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    private void clearRoleCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(ROLE_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private String extractName(OAuth2User principal, String registrationId) {
        if ("linkedin".equalsIgnoreCase(registrationId)) {
            String givenName = principal.getAttribute("given_name");
            String familyName = principal.getAttribute("family_name");
            if (givenName != null && familyName != null) {
                return givenName + " " + familyName;
            }
        }
        String name = principal.getAttribute("name");
        return name != null ? name : "";
    }

    private AuthProvider mapProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "linkedin" -> AuthProvider.LINKEDIN;
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }
}
