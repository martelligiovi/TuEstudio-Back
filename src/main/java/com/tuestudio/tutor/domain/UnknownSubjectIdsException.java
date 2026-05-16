package com.tuestudio.tutor.domain;

import java.util.Set;
import java.util.UUID;

public final class UnknownSubjectIdsException extends RuntimeException {

    private final Set<UUID> unknown;

    public UnknownSubjectIdsException(Set<UUID> unknown) {
        super("Unknown subject ids: " + unknown);
        this.unknown = Set.copyOf(unknown);
    }

    public Set<UUID> unknown() {
        return unknown;
    }
}
