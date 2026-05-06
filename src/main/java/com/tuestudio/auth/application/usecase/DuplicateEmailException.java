package com.tuestudio.auth.application.usecase;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Email already registered: " + email);
    }
}
