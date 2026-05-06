package com.tuestudio.auth.domain;

import java.util.UUID;

public final class User {
    private final UUID id;
    private final String name;
    private final String email;
    private final HashedPassword password;
    private final Role role;

    public User(UUID id, String name, String email, HashedPassword password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static User create(String name, String email, HashedPassword password, Role role) {
        return new User(UUID.randomUUID(), name, email, password, role);
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public String email() { return email; }
    public HashedPassword password() { return password; }
    public Role role() { return role; }
}
