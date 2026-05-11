package com.tuestudio.auth.infrastructure.security;

import com.tuestudio.auth.application.port.TokenPort;
import com.tuestudio.auth.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenAdapter implements TokenPort {

    private final JwtProperties properties;

    public JwtTokenAdapter(JwtProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generate(User user) {
        SecretKey key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(user.email())
                .claim("userId", user.id().toString())
                .claim("role", user.role().name())
                .claim("name", user.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + properties.expirationMs()))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        SecretKey key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }
}
