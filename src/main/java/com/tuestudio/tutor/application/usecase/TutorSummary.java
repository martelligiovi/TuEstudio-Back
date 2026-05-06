package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.domain.TutorId;
import java.util.List;

public record TutorSummary(
        TutorId id,
        String name,
        String university,
        List<String> subjects,
        double hourlyRate,
        boolean active,
        String photoUrl
) {}
