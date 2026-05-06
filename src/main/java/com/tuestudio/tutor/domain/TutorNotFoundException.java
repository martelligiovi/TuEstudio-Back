package com.tuestudio.tutor.domain;

public class TutorNotFoundException extends RuntimeException {
    public TutorNotFoundException(TutorId id) {
        super("Tutor not found: " + id);
    }
}
