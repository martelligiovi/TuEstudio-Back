package com.tuestudio.auth.application.usecase;

public interface SocialAuthUseCase {
    AuthResult authenticate(SocialAuthCommand command);
}
