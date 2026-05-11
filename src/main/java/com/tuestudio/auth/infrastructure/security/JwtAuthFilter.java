package com.tuestudio.auth.infrastructure.security;

import com.tuestudio.auth.application.port.UserRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenAdapter jwtTokenAdapter;
    private final UserRepositoryPort userRepository;

    public JwtAuthFilter(JwtTokenAdapter jwtTokenAdapter, UserRepositoryPort userRepository) {
        this.jwtTokenAdapter = jwtTokenAdapter;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String email = jwtTokenAdapter.extractEmail(token);
                userRepository.findByEmail(email).ifPresent(user -> {
                    var auth = new UsernamePasswordAuthenticationToken(
                            user, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            } catch (Exception ignored) {}
        }
        filterChain.doFilter(request, response);
    }
}
