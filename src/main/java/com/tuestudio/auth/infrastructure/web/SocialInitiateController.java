package com.tuestudio.auth.infrastructure.web;

import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.infrastructure.security.CookieSigningService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Set;

@RestController
@RequestMapping("/api/auth/social")
public class SocialInitiateController {

    private static final Set<String> SUPPORTED_PROVIDERS = Set.of("google", "linkedin");
    private static final Set<Role> SOCIAL_AUTH_ROLES = Set.of(Role.STUDENT, Role.TEACHER);
    private static final int COOKIE_MAX_AGE_SECONDS = 300;
    private static final String ROLE_COOKIE_NAME = "oauth2_role";

    private final CookieSigningService cookieSigningService;
    private final String appBaseUrl;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public SocialInitiateController(
            CookieSigningService cookieSigningService,
            @Value("${app.base-url}") String appBaseUrl,
            @Value("${app.cookie.secure:false}") boolean cookieSecure,
            @Value("${app.cookie.same-site:Lax}") String cookieSameSite) {
        this.cookieSigningService = cookieSigningService;
        this.appBaseUrl = appBaseUrl;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite == null || cookieSameSite.isBlank() ? "Lax" : cookieSameSite;
    }

    @GetMapping("/initiate")
    public ResponseEntity<Void> initiate(
            @RequestParam String provider,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "register") String flow,
            HttpServletResponse response) {

        String normalizedProvider = provider.toLowerCase();
        if (!SUPPORTED_PROVIDERS.contains(normalizedProvider)) {
            return ResponseEntity.badRequest().build();
        }

        String cookieValue;
        if ("login".equalsIgnoreCase(flow)) {
            cookieValue = cookieSigningService.signLogin();
        } else {
            if (role == null) {
                return ResponseEntity.badRequest().build();
            }
            try {
                Role parsed = Role.valueOf(role.toUpperCase());
                if (!SOCIAL_AUTH_ROLES.contains(parsed)) {
                    return ResponseEntity.badRequest().build();
                }
                cookieValue = cookieSigningService.sign(parsed);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        addRoleCookie(response, cookieValue);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, appBaseUrl + "/oauth2/authorization/" + normalizedProvider)
                .build();
    }

    private void addRoleCookie(HttpServletResponse response, String cookieValue) {
        ResponseCookie cookie = ResponseCookie.from(ROLE_COOKIE_NAME, cookieValue)
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .maxAge(Duration.ofSeconds(COOKIE_MAX_AGE_SECONDS))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
