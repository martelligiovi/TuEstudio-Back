package com.tuestudio.auth.infrastructure.security;

import com.tuestudio.auth.application.port.TokenPort;
import com.tuestudio.auth.application.port.UserRepositoryPort;
import com.tuestudio.auth.domain.HashedPassword;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleOAuthService {

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final GoogleOAuthProperties properties;
    private final UserRepositoryPort userRepository;
    private final TokenPort tokenPort;
    private final RestTemplate restTemplate;

    public GoogleOAuthService(GoogleOAuthProperties properties, UserRepositoryPort userRepository,
                               TokenPort tokenPort, RestTemplate restTemplate) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.tokenPort = tokenPort;
        this.restTemplate = restTemplate;
    }

    public String buildAuthorizationUrl(String flow) {
        String state = Base64.getUrlEncoder().encodeToString(flow.getBytes(StandardCharsets.UTF_8));
        return AUTH_URL
                + "?client_id=" + properties.clientId()
                + "&redirect_uri=" + properties.redirectUri()
                + "&response_type=code"
                + "&scope=openid%20email%20profile"
                + "&state=" + state
                + "&access_type=offline"
                + "&prompt=consent";
    }

    public OAuthCallbackResult handleCallback(String code, String state) {
        if (code == null || code.isBlank()) {
            return new OAuthCallbackResult.Failure("missing_code");
        }

        String decodedState;
        try {
            decodedState = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return new OAuthCallbackResult.Failure("invalid_state");
        }

        String accessToken;
        try {
            accessToken = exchangeCodeForToken(code);
        } catch (RestClientException e) {
            return new OAuthCallbackResult.Failure("token_exchange_failed");
        }
        if (accessToken == null) {
            return new OAuthCallbackResult.Failure("token_exchange_failed");
        }

        Map<String, Object> userInfo;
        try {
            userInfo = fetchUserInfo(accessToken);
        } catch (RestClientException e) {
            return new OAuthCallbackResult.Failure("userinfo_failed");
        }
        if (userInfo == null) {
            return new OAuthCallbackResult.Failure("userinfo_failed");
        }

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        if (email == null || email.isBlank()) {
            return new OAuthCallbackResult.Failure("userinfo_failed");
        }

        return resolveUser(decodedState, email, name)
                .map(user -> (OAuthCallbackResult) new OAuthCallbackResult.Success(tokenPort.generate(user)))
                .orElseGet(() -> new OAuthCallbackResult.Failure(failureReason(decodedState)));
    }

    private Optional<User> resolveUser(String decodedState, String email, String name) {
        Optional<User> existing = userRepository.findByEmail(email);

        if (decodedState.equals("login")) {
            return existing;
        }

        if (decodedState.startsWith("register-")) {
            if (existing.isPresent()) {
                return existing;
            }
            Role role = mapRole(decodedState.substring("register-".length()));
            if (role == null) {
                return Optional.empty();
            }
            User newUser = User.create(name, email,
                    new HashedPassword(UUID.randomUUID().toString()), role);
            userRepository.save(newUser);
            return Optional.of(newUser);
        }

        return Optional.empty();
    }

    private Role mapRole(String raw) {
        return switch (raw.toUpperCase()) {
            case "STUDENT" -> Role.STUDENT;
            case "TEACHER", "TUTOR" -> Role.TEACHER;
            default -> null;
        };
    }

    private String failureReason(String decodedState) {
        if (decodedState.equals("login")) return "user_not_found";
        if (decodedState.startsWith("register-")) return "unknown_role";
        return "invalid_state";
    }

    @SuppressWarnings("unchecked")
    private String exchangeCodeForToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", properties.clientId());
        body.add("client_secret", properties.clientSecret());
        body.add("redirect_uri", properties.redirectUri());
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> response = restTemplate.exchange(
                TOKEN_URL, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

        Map<String, Object> responseBody = response.getBody();
        return responseBody == null ? null : (String) responseBody.get("access_token");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<Map> response = restTemplate.exchange(
                USERINFO_URL, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        return response.getBody();
    }
}
