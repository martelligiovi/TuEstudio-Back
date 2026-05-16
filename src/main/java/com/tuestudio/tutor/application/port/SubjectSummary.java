package com.tuestudio.tutor.application.port;

import java.util.Objects;
import java.util.UUID;

public record SubjectSummary(UUID id, String canonicalName, String icon) {

    public SubjectSummary {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(canonicalName, "canonicalName must not be null");
        // icon is nullable — subject may not have an icon configured
    }
}
