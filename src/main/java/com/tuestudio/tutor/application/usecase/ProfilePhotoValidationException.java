package com.tuestudio.tutor.application.usecase;

public class ProfilePhotoValidationException extends RuntimeException {
    private final String code;

    public ProfilePhotoValidationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
