package com.tuestudio.auth.application.usecase;

public interface PasswordHasher {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}
