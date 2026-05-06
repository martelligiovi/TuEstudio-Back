package com.tuestudio.auth.infrastructure.persistence;

import com.tuestudio.auth.domain.HashedPassword;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users")
class UserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected UserJpaEntity() {}

    static UserJpaEntity fromDomain(User user) {
        UserJpaEntity e = new UserJpaEntity();
        e.id = user.id();
        e.name = user.name();
        e.email = user.email();
        e.hashedPassword = user.password().value();
        e.role = user.role();
        return e;
    }

    User toDomain() {
        return new User(id, name, email, new HashedPassword(hashedPassword), role);
    }
}
