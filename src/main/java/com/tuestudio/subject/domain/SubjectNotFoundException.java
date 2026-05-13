package com.tuestudio.subject.domain;

public class SubjectNotFoundException extends RuntimeException {
    public SubjectNotFoundException(SubjectId id) {
        super("Subject not found: " + id.value());
    }
}
