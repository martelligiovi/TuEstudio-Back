package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.application.port.TokenPort;
import com.tuestudio.auth.application.port.TutorProvisioningPort;
import com.tuestudio.auth.application.port.UserRepositoryPort;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import org.springframework.transaction.annotation.Transactional;

public class SocialAuthService implements SocialAuthUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenPort tokenPort;
    private final TutorProvisioningPort tutorProvisioning;

    public SocialAuthService(UserRepositoryPort userRepository, TokenPort tokenPort,
                             TutorProvisioningPort tutorProvisioning) {
        this.userRepository = userRepository;
        this.tokenPort = tokenPort;
        this.tutorProvisioning = tutorProvisioning;
    }

    @Override
    @Transactional
    public AuthResult authenticate(SocialAuthCommand command) {
        User user = command.role().isPresent()
                ? userRepository
                        .findByProviderAndProviderUserId(command.provider(), command.providerUserId())
                        .orElseGet(() -> createAndSave(command))
                : userRepository
                        .findByProviderAndProviderUserId(command.provider(), command.providerUserId())
                        .orElseThrow(() -> new UserNotFoundException(
                                "No account found for provider %s".formatted(command.provider())));

        String token = tokenPort.generate(user);
        return new AuthResult(token, user.id(), user.name(), user.email(), user.role());
    }

    private User createAndSave(SocialAuthCommand command) {
        User newUser = User.createSocial(
                command.name(),
                command.email(),
                command.provider(),
                command.providerUserId(),
                command.role().orElseThrow()
        );
        userRepository.save(newUser);

        if (newUser.role() == Role.TEACHER) {
            tutorProvisioning.provisionFor(newUser);
        }
        return newUser;
    }
}
