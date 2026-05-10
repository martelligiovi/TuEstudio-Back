package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.application.port.TokenPort;
import com.tuestudio.auth.application.port.UserRepositoryPort;
import com.tuestudio.auth.domain.User;

public final class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenPort tokenPort;
    private final PasswordHasher passwordHasher;

    public LoginService(UserRepositoryPort userRepository, TokenPort tokenPort, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.tokenPort = tokenPort;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public AuthResult login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (user.isSocial()) {
            throw new InvalidCredentialsException();
        }
        if (!passwordHasher.matches(command.rawPassword(), user.password().value())) {
            throw new InvalidCredentialsException();
        }
        String token = tokenPort.generate(user);
        return new AuthResult(token, user.id(), user.name(), user.email(), user.role());
    }
}
