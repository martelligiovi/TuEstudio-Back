package com.tuestudio.auth.application.usecase;

public interface RegisterUseCase {
    AuthResult register(RegisterCommand command);
}
