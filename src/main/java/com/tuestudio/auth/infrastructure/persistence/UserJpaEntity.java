package com.tuestudio.auth.infrastructure.persistence;

import com.tuestudio.auth.domain.AuthProvider;
import com.tuestudio.auth.domain.HashedPassword;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = true)
    private String providerUserId;

    protected UserJpaEntity() {}

    public static UserJpaEntity fromDomain(User user) {
        UserJpaEntity e = new UserJpaEntity();
        e.id = user.id();
        e.name = user.name();
        e.email = user.email();
        e.role = user.role();
        e.provider = user.provider().name();
        e.providerUserId = user.providerUserId();
        e.hashedPassword = user.optionalPassword().map(HashedPassword::value).orElse(null);
        return e;
    }

    User toDomain() {
        AuthProvider authProvider = AuthProvider.valueOf(provider);
        if (authProvider == AuthProvider.LOCAL) {
            return new User(id, name, email, new HashedPassword(hashedPassword), role);
        }
        return User.reconstructSocial(id, name, email, authProvider, providerUserId, role);
    }

    // Accessors for testing and cross-package seeder usage
    public UUID getId() { return id; }
    String getHashedPassword() { return hashedPassword; }
    public String getProvider() { return provider; }
    public String getProviderUserId() { return providerUserId; }
}
