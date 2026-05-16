package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.tutor.application.port.SubjectSummary;
import com.tuestudio.tutor.application.usecase.TutorSummary;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TutorSummaryResponse(
        String id,
        String name,
        String university,
        List<SubjectDto> subjects,
        double hourlyRate,
        boolean active,
        String photoUrl
) {

    public record SubjectDto(UUID id, String canonicalName, String icon) {}

    static TutorSummaryResponse from(TutorSummary s, Map<UUID, SubjectSummary> subjectIndex) {
        List<SubjectDto> subjects = s.subjects().stream()
                .filter(subjectIndex::containsKey)
                .map(uuid -> {
                    SubjectSummary ss = subjectIndex.get(uuid);
                    return new SubjectDto(ss.id(), ss.canonicalName(), ss.icon());
                })
                .toList();
        return new TutorSummaryResponse(
                s.id().value().toString(), s.name(), s.university(),
                subjects, s.hourlyRate(), s.active(), s.photoUrl());
    }
}
