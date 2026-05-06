package com.tuestudio.auth.domain;

public record HashedPassword(String value) {
    public HashedPassword {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Hashed password cannot be blank");
        }
    }
}
