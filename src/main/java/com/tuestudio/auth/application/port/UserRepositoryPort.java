package com.tuestudio.auth.application.port;

import com.tuestudio.auth.domain.User;
import java.util.Optional;

public interface UserRepositoryPort {
    void save(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
