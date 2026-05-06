package com.tuestudio.tutor.domain;

import java.util.UUID;

public record TutorId(UUID value) {
    public static TutorId of(UUID value) { return new TutorId(value); }
    public static TutorId generate() { return new TutorId(UUID.randomUUID()); }
    @Override public String toString() { return value.toString(); }
}
