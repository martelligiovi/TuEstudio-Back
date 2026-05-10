package com.tuestudio.auth.infrastructure.web;

import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.infrastructure.security.CookieSigningService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/auth/social")
public class SocialInitiateController {

    private static final Set<String> SUPPORTED_PROVIDERS = Set.of("google", "linkedin");
    private static final int COOKIE_MAX_AGE_SECONDS = 300;
    private static final String ROLE_COOKIE_NAME = "oauth2_role";

    private final CookieSigningService cookieSigningService;
    private final String appBaseUrl;

    public SocialInitiateController(
            CookieSigningService cookieSigningService,
            @Value("${app.base-url}") String appBaseUrl) {
        this.cookieSigningService = cookieSigningService;
        this.appBaseUrl = appBaseUrl;
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
                cookieValue = cookieSigningService.sign(Role.valueOf(role.toUpperCase()));
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
        Cookie cookie = new Cookie(ROLE_COOKIE_NAME, cookieValue);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        cookie.setPath("/");
        // SameSite=Lax must be set via Set-Cookie header directly (Servlet Cookie API doesn't support it)
        response.addCookie(cookie);
        // Override to add SameSite attribute
        String cookieHeader = ROLE_COOKIE_NAME + "=" + cookieValue
                + "; Path=/"
                + "; Max-Age=" + COOKIE_MAX_AGE_SECONDS
                + "; HttpOnly"
                + "; SameSite=Lax";
        response.setHeader("Set-Cookie", cookieHeader);
    }
}
