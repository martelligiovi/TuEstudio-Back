package com.tuestudio.auth.domain;

import java.util.Optional;
import java.util.UUID;

public final class User {
    private final UUID id;
    private final String name;
    private final String email;
    private final HashedPassword password;
    private final Role role;
    private final AuthProvider provider;
    private final String providerUserId;

    public User(UUID id, String name, String email, HashedPassword password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.provider = AuthProvider.LOCAL;
        this.providerUserId = null;
    }

    // Package-private constructor for social users — no HashedPassword validation
    User(UUID id, String name, String email, AuthProvider provider, String providerUserId, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = null;
        this.role = role;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    public static User create(String name, String email, HashedPassword password, Role role) {
        return new User(UUID.randomUUID(), name, email, password, role);
    }

    public static User createSocial(String name, String email, AuthProvider provider,
                                    String providerUserId, Role role) {
        return new User(UUID.randomUUID(), name, email, provider, providerUserId, role);
    }

    /**
     * Reconstructs a social User from persistence with its existing identity.
     * Only for use by the persistence adapter.
     */
    public static User reconstructSocial(UUID id, String name, String email,
                                         AuthProvider provider, String providerUserId, Role role) {
        return new User(id, name, email, provider, providerUserId, role);
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public String email() { return email; }
    public HashedPassword password() { return password; }
    public Optional<HashedPassword> optionalPassword() { return Optional.ofNullable(password); }
    public Role role() { return role; }
    public AuthProvider provider() { return provider; }
    public String providerUserId() { return providerUserId; }
    public boolean isSocial() { return provider != AuthProvider.LOCAL; }
}
