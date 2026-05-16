package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.domain.TutorId;
import java.util.List;
import java.util.UUID;

public record TutorSummary(
        TutorId id,
        String name,
        String university,
        List<UUID> subjects,
        double hourlyRate,
        boolean active,
        String photoUrl
) {}
