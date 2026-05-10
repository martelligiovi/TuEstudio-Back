package com.tuestudio.auth.infrastructure.persistence;

import com.tuestudio.auth.application.port.UserRepositoryPort;
import com.tuestudio.auth.domain.AuthProvider;
import com.tuestudio.auth.domain.User;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class UserJpaAdapter implements UserRepositoryPort {

    private final UserJpaRepository repository;

    public UserJpaAdapter(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(User user) {
        repository.save(UserJpaEntity.fromDomain(user));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(UserJpaEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return repository.findByProviderAndProviderUserId(provider.name(), providerUserId)
                .map(UserJpaEntity::toDomain);
    }
}
