package com.tuestudio.subject.domain;

public class SubjectAlreadyExistsException extends RuntimeException {
    public SubjectAlreadyExistsException(String canonicalName) {
        super("Subject already exists: " + canonicalName);
    }
}
