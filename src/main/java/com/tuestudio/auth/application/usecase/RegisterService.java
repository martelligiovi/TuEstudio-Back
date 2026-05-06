package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.application.port.TokenPort;
import com.tuestudio.auth.application.port.UserRepositoryPort;
import com.tuestudio.auth.domain.HashedPassword;
import com.tuestudio.auth.domain.User;

public final class RegisterService implements RegisterUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenPort tokenPort;
    private final PasswordHasher passwordHasher;

    public RegisterService(UserRepositoryPort userRepository, TokenPort tokenPort, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.tokenPort = tokenPort;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public AuthResult register(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException(command.email());
        }
        HashedPassword hashed = new HashedPassword(passwordHasher.hash(command.rawPassword()));
        User user = User.create(command.name(), command.email(), hashed, command.role());
        userRepository.save(user);
        String token = tokenPort.generate(user);
        return new AuthResult(token, user.id(), user.name(), user.email(), user.role());
    }
}
