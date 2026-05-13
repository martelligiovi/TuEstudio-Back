package com.tuestudio.subject.infrastructure.web;

import com.tuestudio.subject.domain.Subject;

import java.util.List;
import java.util.UUID;

public record SubjectResponse(UUID id, String canonicalName, List<String> aliases) {

    public static SubjectResponse from(Subject s) {
        return new SubjectResponse(s.id().value(), s.canonicalName(), List.copyOf(s.aliases()));
    }
}
