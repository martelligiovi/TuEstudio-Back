package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.tutor.application.usecase.TutorSummary;
import java.util.List;

public record TutorSummaryResponse(
        String id,
        String name,
        String university,
        List<String> subjects,
        double hourlyRate,
        boolean active,
        String photoUrl
) {
    static TutorSummaryResponse from(TutorSummary s) {
        return new TutorSummaryResponse(
                s.id().value().toString(), s.name(), s.university(),
                s.subjects(), s.hourlyRate(), s.active(), s.photoUrl());
    }
}
