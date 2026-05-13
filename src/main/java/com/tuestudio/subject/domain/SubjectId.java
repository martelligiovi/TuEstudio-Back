package com.tuestudio.subject.domain;

import java.util.Objects;
import java.util.UUID;

public record SubjectId(UUID value) {

    public SubjectId {
        Objects.requireNonNull(value);
    }

    public static SubjectId of(UUID value) {
        return new SubjectId(value);
    }

    public static SubjectId newId() {
        return new SubjectId(UUID.randomUUID());
    }
}
