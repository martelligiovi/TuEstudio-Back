package com.tuestudio.tutor.domain;

import java.util.Objects;
import java.util.UUID;

public record AssignedSubjectId(UUID value) {

    public AssignedSubjectId {
        Objects.requireNonNull(value, "subject id required");
    }

    public static AssignedSubjectId of(UUID value) {
        return new AssignedSubjectId(value);
    }
}
