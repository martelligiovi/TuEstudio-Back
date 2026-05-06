package com.tuestudio.auth.application.usecase;

public interface LoginUseCase {
    AuthResult login(LoginCommand command);
}
