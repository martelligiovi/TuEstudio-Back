package com.tuestudio.auth.infrastructure.security;

import com.tuestudio.auth.application.usecase.AuthResult;
import com.tuestudio.auth.application.usecase.SocialAuthCommand;
import com.tuestudio.auth.application.usecase.SocialAuthUseCase;
import com.tuestudio.auth.application.usecase.UserNotFoundException;
import com.tuestudio.auth.domain.AuthProvider;
import com.tuestudio.auth.domain.Role;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private SocialAuthUseCase socialAuthUseCase;

    @Mock
    private CookieSigningService cookieSigningService;

    private OAuth2LoginSuccessHandler handler;

    private static final String FRONTEND_REDIRECT = "http://localhost:5173/oauth2/callback";
    private static final String ERROR_REDIRECT = "http://localhost:5173/oauth2/error";
    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        handler = new OAuth2LoginSuccessHandler(socialAuthUseCase, cookieSigningService, FRONTEND_REDIRECT);
    }

    @Test
    void happyPath_google_redirectsWithToken() throws Exception {
        // Given: valid signed cookie
        when(cookieSigningService.verify("STUDENT.validsig")).thenReturn(Optional.of(Role.STUDENT));

        // And: SocialAuthUseCase returns an AuthResult
        AuthResult authResult = new AuthResult("jwt-token-123", USER_ID, "Ana Garcia", "ana@gmail.com", Role.STUDENT);
        when(socialAuthUseCase.authenticate(any(SocialAuthCommand.class))).thenReturn(authResult);

        MockHttpServletRequest request = buildRequest("STUDENT.validsig");
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationToken auth = buildGoogleAuth("google-sub-123", "ana@gmail.com", "Ana Garcia");

        handler.onAuthenticationSuccess(request, response, auth);

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_REDIRECT + "?token=jwt-token-123");
        // Cookie should be cleared
        Cookie clearedCookie = response.getCookie("oauth2_role");
        assertThat(clearedCookie).isNotNull();
        assertThat(clearedCookie.getMaxAge()).isZero();
    }

    @Test
    void happyPath_google_callsUseCaseWithCorrectCommand() throws Exception {
        when(cookieSigningService.verify("STUDENT.validsig")).thenReturn(Optional.of(Role.STUDENT));
        AuthResult authResult = new AuthResult("jwt-token-123", USER_ID, "Ana Garcia", "ana@gmail.com", Role.STUDENT);
        when(socialAuthUseCase.authenticate(any(SocialAuthCommand.class))).thenReturn(authResult);

        MockHttpServletRequest request = buildRequest("STUDENT.validsig");
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationToken auth = buildGoogleAuth("google-sub-123", "ana@gmail.com", "Ana Garcia");

        handler.onAuthenticationSuccess(request, response, auth);

        ArgumentCaptor<SocialAuthCommand> captor = ArgumentCaptor.forClass(SocialAuthCommand.class);
        verify(socialAuthUseCase).authenticate(captor.capture());
        SocialAuthCommand cmd = captor.getValue();
        assertThat(cmd.provider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(cmd.providerUserId()).isEqualTo("google-sub-123");
        assertThat(cmd.email()).isEqualTo("ana@gmail.com");
        assertThat(cmd.name()).isEqualTo("Ana Garcia");
        assertThat(cmd.role()).isEqualTo(Optional.of(Role.STUDENT));
    }

    @Test
    void happyPath_linkedin_concatenatesGivenAndFamilyName() throws Exception {
        when(cookieSigningService.verify("TEACHER.validsig")).thenReturn(Optional.of(Role.TEACHER));
        AuthResult authResult = new AuthResult("jwt-token-456", USER_ID, "Carlos Lopez", "carlos@linkedin.com", Role.TEACHER);
        when(socialAuthUseCase.authenticate(any(SocialAuthCommand.class))).thenReturn(authResult);

        MockHttpServletRequest request = buildRequest("TEACHER.validsig");
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationToken auth = buildLinkedInAuth("li-sub-456", "carlos@linkedin.com", "Carlos", "Lopez");

        handler.onAuthenticationSuccess(request, response, auth);

        ArgumentCaptor<SocialAuthCommand> captor = ArgumentCaptor.forClass(SocialAuthCommand.class);
        verify(socialAuthUseCase).authenticate(captor.capture());
        SocialAuthCommand cmd = captor.getValue();
        assertThat(cmd.provider()).isEqualTo(AuthProvider.LINKEDIN);
        assertThat(cmd.name()).isEqualTo("Carlos Lopez");
        assertThat(cmd.email()).isEqualTo("carlos@linkedin.com");
    }

    @Test
    void missingRoleCookie_redirectsToError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // No cookie set
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationToken auth = buildGoogleAuth("google-sub-123", "ana@gmail.com", "Ana Garcia");

        handler.onAuthenticationSuccess(request, response, auth);

        assertThat(response.getRedirectedUrl()).startsWith(ERROR_REDIRECT);
        assertThat(response.getRedirectedUrl()).contains("reason=invalid_role");
        verify(socialAuthUseCase, never()).authenticate(any());
    }

    @Test
    void tamperedCookie_redirectsToError() throws Exception {
        when(cookieSigningService.verify("STUDENT.tampered")).thenReturn(Optional.empty());

        MockHttpServletRequest request = buildRequest("STUDENT.tampered");
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationToken auth = buildGoogleAuth("google-sub-123", "ana@gmail.com", "Ana Garcia");

        handler.onAuthenticationSuccess(request, response, auth);

        assertThat(response.getRedirectedUrl()).startsWith(ERROR_REDIRECT);
        assertThat(response.getRedirectedUrl()).contains("reason=invalid_role");
        verify(socialAuthUseCase, never()).authenticate(any());
    }

    @Test
    void missingEmail_redirectsToError() throws Exception {
        when(cookieSigningService.verify("STUDENT.validsig")).thenReturn(Optional.of(Role.STUDENT));

        MockHttpServletRequest request = buildRequest("STUDENT.validsig");
        MockHttpServletResponse response = new MockHttpServletResponse();
        // No email in attributes
        OAuth2AuthenticationToken auth = buildGoogleAuthNoEmail("google-sub-123", "Ana Garcia");

        handler.onAuthenticationSuccess(request, response, auth);

        assertThat(response.getRedirectedUrl()).startsWith(ERROR_REDIRECT);
        assertThat(response.getRedirectedUrl()).contains("reason=missing_email");
        verify(socialAuthUseCase, never()).authenticate(any());
    }

    @Test
    void clearsCookie_afterSuccess() throws Exception {
        when(cookieSigningService.verify("STUDENT.validsig")).thenReturn(Optional.of(Role.STUDENT));
        AuthResult authResult = new AuthResult("jwt-token-123", USER_ID, "Ana Garcia", "ana@gmail.com", Role.STUDENT);
        when(socialAuthUseCase.authenticate(any())).thenReturn(authResult);

        MockHttpServletRequest request = buildRequest("STUDENT.validsig");
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationToken auth = buildGoogleAuth("google-sub-123", "ana@gmail.com", "Ana Garcia");

        handler.onAuthenticationSuccess(request, response, auth);

        Cookie clearedCookie = response.getCookie("oauth2_role");
        assertThat(clearedCookie).isNotNull();
        assertThat(clearedCookie.getMaxAge()).isZero();
    }

    @Test
    void loginFlow_existingAccount_redirectsWithToken() throws Exception {
        when(cookieSigningService.isLoginFlow("LOGIN.validsig")).thenReturn(true);
        when(cookieSigningService.verify("LOGIN.validsig")).thenReturn(Optional.empty());
        AuthResult authResult = new AuthResult("jwt-login-token", USER_ID, "Ana Garcia", "ana@gmail.com", Role.STUDENT);
        when(socialAuthUseCase.authenticate(any(SocialAuthCommand.class))).thenReturn(authResult);

        MockHttpServletRequest request = buildRequest("LOGIN.validsig");
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationToken auth = buildGoogleAuth("google-sub-123", "ana@gmail.com", "Ana Garcia");

        handler.onAuthenticationSuccess(request, response, auth);

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_REDIRECT + "?token=jwt-login-token");
    }

    @Test
    void loginFlow_callsUseCaseWithEmptyRole() throws Exception {
        when(cookieSigningService.isLoginFlow("LOGIN.validsig")).thenReturn(true);
        when(cookieSigningService.verify("LOGIN.validsig")).thenReturn(Optional.empty());
        AuthResult authResult = new AuthResult("jwt-login-token", USER_ID, "Ana Garcia", "ana@gmail.com", Role.STUDENT);
        when(socialAuthUseCase.authenticate(any(SocialAuthCommand.class))).thenReturn(authResult);

        MockHttpServletRequest request = buildRequest("LOGIN.validsig");
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationToken auth = buildGoogleAuth("google-sub-123", "ana@gmail.com", "Ana Garcia");

        handler.onAuthenticationSuccess(request, response, auth);

        ArgumentCaptor<SocialAuthCommand> captor = ArgumentCaptor.forClass(SocialAuthCommand.class);
        verify(socialAuthUseCase).authenticate(captor.capture());
        assertThat(captor.getValue().role()).isEmpty();
    }

    @Test
    void loginFlow_unknownAccount_redirectsToError() throws Exception {
        when(cookieSigningService.isLoginFlow("LOGIN.validsig")).thenReturn(true);
        when(cookieSigningService.verify("LOGIN.validsig")).thenReturn(Optional.empty());
        when(socialAuthUseCase.authenticate(any(SocialAuthCommand.class)))
                .thenThrow(new UserNotFoundException("No account found"));

        MockHttpServletRequest request = buildRequest("LOGIN.validsig");
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationToken auth = buildGoogleAuth("google-sub-123", "ana@gmail.com", "Ana Garcia");

        handler.onAuthenticationSuccess(request, response, auth);

        assertThat(response.getRedirectedUrl()).contains("reason=account_not_found");
    }

    // --- Helpers ---

    private MockHttpServletRequest buildRequest(String cookieValue) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cookie cookie = new Cookie("oauth2_role", cookieValue);
        request.setCookies(cookie);
        return request;
    }

    private OAuth2AuthenticationToken buildGoogleAuth(String sub, String email, String name) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("sub", sub);
        attrs.put("email", email);
        attrs.put("name", name);
        OAuth2User user = new DefaultOAuth2User(List.of(), attrs, "sub");
        return new OAuth2AuthenticationToken(user, List.of(), "google");
    }

    private OAuth2AuthenticationToken buildGoogleAuthNoEmail(String sub, String name) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("sub", sub);
        attrs.put("name", name);
        OAuth2User user = new DefaultOAuth2User(List.of(), attrs, "sub");
        return new OAuth2AuthenticationToken(user, List.of(), "google");
    }

    private OAuth2AuthenticationToken buildLinkedInAuth(String sub, String email, String givenName, String familyName) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("sub", sub);
        attrs.put("email", email);
        attrs.put("given_name", givenName);
        attrs.put("family_name", familyName);
        OAuth2User user = new DefaultOAuth2User(List.of(), attrs, "sub");
        return new OAuth2AuthenticationToken(user, List.of(), "linkedin");
    }
}
