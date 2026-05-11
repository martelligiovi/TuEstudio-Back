package com.tuestudio.auth.infrastructure.web;

import com.tuestudio.auth.infrastructure.security.GoogleOAuthService;
import com.tuestudio.auth.infrastructure.security.OAuthCallbackResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/social")
public class SocialAuthController {

    private final GoogleOAuthService googleOAuthService;
    private final String frontendUrl;

    public SocialAuthController(GoogleOAuthService googleOAuthService,
                                 @Value("${app.frontend-url}") String frontendUrl) {
        this.googleOAuthService = googleOAuthService;
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/initiate")
    public ResponseEntity<Map<String, String>> initiate(
            @RequestParam(defaultValue = "google") String provider,
            @RequestParam(defaultValue = "login") String flow) {
        String url = googleOAuthService.buildAuthorizationUrl(flow);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false, defaultValue = "login") String state) {

        if (error != null && !error.isBlank()) {
            return redirect(errorUrl(error));
        }

        OAuthCallbackResult result = googleOAuthService.handleCallback(code, state);
        URI target = switch (result) {
            case OAuthCallbackResult.Success s -> successUrl(s.token());
            case OAuthCallbackResult.Failure f -> errorUrl(f.reason());
        };
        return redirect(target);
    }

    private URI successUrl(String token) {
        return UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/oauth2/callback")
                .queryParam("token", token)
                .build()
                .toUri();
    }

    private URI errorUrl(String reason) {
        return UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/oauth2/error")
                .queryParam("reason", reason)
                .build()
                .toUri();
    }

    private ResponseEntity<Void> redirect(URI uri) {
        return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
    }
}
