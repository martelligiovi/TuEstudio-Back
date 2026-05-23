package com.tuestudio.auth.infrastructure.security;

import com.tuestudio.auth.application.port.TutorProvisioningPort;
import com.tuestudio.auth.application.port.UserRepositoryPort;
import com.tuestudio.auth.application.usecase.LoginService;
import com.tuestudio.auth.application.usecase.LoginUseCase;
import com.tuestudio.auth.application.usecase.PasswordHasher;
import com.tuestudio.auth.application.usecase.RegisterService;
import com.tuestudio.auth.application.usecase.RegisterUseCase;
import com.tuestudio.auth.application.usecase.SocialAuthService;
import com.tuestudio.auth.application.usecase.SocialAuthUseCase;
import com.tuestudio.auth.application.port.TokenPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
            CookieOAuth2AuthorizationRequestRepository authorizationRequestRepository,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestRepository(authorizationRequestRepository)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**")
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/tutors/**").permitAll()
                        .requestMatchers("/api/catalog").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/subjects/**").permitAll()
                        .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtTokenAdapter jwtTokenAdapter, UserRepositoryPort userRepository) {
        return new JwtAuthFilter(jwtTokenAdapter, userRepository);
    }

    @Bean
    public RegisterUseCase registerUseCase(UserRepositoryPort userRepository, TokenPort tokenPort,
                                            PasswordHasher passwordHasher, TutorProvisioningPort tutorProvisioning) {
        return new RegisterService(userRepository, tokenPort, passwordHasher, tutorProvisioning);
    }

    @Bean
    public LoginUseCase loginUseCase(UserRepositoryPort userRepository, TokenPort tokenPort, PasswordHasher passwordHasher) {
        return new LoginService(userRepository, tokenPort, passwordHasher);
    }

    @Bean
    public SocialAuthUseCase socialAuthUseCase(UserRepositoryPort userRepository, TokenPort tokenPort,
                                               TutorProvisioningPort tutorProvisioning) {
        return new SocialAuthService(userRepository, tokenPort, tutorProvisioning);
    }

    @Bean
    public CookieSigningService cookieSigningService(OAuth2CookieProperties props) {
        return new CookieSigningService(props);
    }

    @Bean
    public OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler(
            SocialAuthUseCase socialAuthUseCase,
            CookieSigningService cookieSigningService,
            @Value("${oauth2.frontend-redirect-url}") String frontendRedirectUrl) {
        return new OAuth2LoginSuccessHandler(socialAuthUseCase, cookieSigningService, frontendRedirectUrl);
    }

    @Bean
    public CookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository(
            @Value("${app.cookie.secure:false}") boolean secure,
            @Value("${app.cookie.same-site:Lax}") String sameSite) {
        return new CookieOAuth2AuthorizationRequestRepository(secure, sameSite);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(parseAllowedOrigins(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static List<String> parseAllowedOrigins(String allowedOrigins) {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
        return origins.isEmpty() ? List.of("http://localhost:5173") : origins;
    }
}
