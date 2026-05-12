package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.domain.*;

import java.util.List;
import java.util.Objects;

/**
 * Command carrying the 14 client-editable fields for a full-replacement PUT.
 * Fields NOT in this command: id (from JWT), rating (server-managed),
 * reviewsCount (server-managed), active (auto-computed by recomputeActive()).
 */
public record UpdateTutorProfileCommand(
        TutorId id,
        String name,
        String subjectSpecialty,
        String university,
        String location,
        String modalidad,
        String bio,
        String photoUrl,
        double hourlyRate,
        List<Subject> subjects,
        Methodology methodology,
        List<Schedule> schedules,
        String schedulesNote,
        List<Plan> plans,
        String phoneNumber
) {
    public UpdateTutorProfileCommand {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        subjects  = subjects == null ? List.of() : List.copyOf(subjects);
        schedules = schedules == null ? List.of() : List.copyOf(schedules);
        plans     = plans == null ? List.of() : List.copyOf(plans);
        if (methodology == null) methodology = new Methodology("", List.of());
    }
}
