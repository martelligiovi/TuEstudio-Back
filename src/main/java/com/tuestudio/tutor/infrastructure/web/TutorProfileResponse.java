package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.tutor.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for GET and PUT /api/teacher/profile.
 * Contains all 18 Tutor fields plus missingForActivation advisory list.
 * The advisory list tells the frontend which conditions must be met to flip active=true.
 *
 * NOTE (PR 1): subjects are returned as UUID strings. Full enrichment with canonical names
 * will be wired in PR 2 via SubjectLookupPort.
 */
public record TutorProfileResponse(
        UUID id,
        String name,
        String subjectSpecialty,
        String university,
        String location,
        String modalidad,
        double rating,
        int reviewsCount,
        String bio,
        String photoUrl,
        boolean active,
        double hourlyRate,
        List<SubjectDto> subjects,
        MethodologyDto methodology,
        List<ScheduleDto> schedules,
        String schedulesNote,
        List<PlanDto> plans,
        String phoneNumber,
        List<String> missingForActivation
) {
    /** PR 1: minimal subject DTO carrying only the UUID. Enriched with canonicalName in PR 2. */
    record SubjectDto(UUID id) {}
    record ScheduleDto(String days, String hours) {}
    record PlanDto(String name, String description, String price, String unit, String badge, boolean featured) {}
    record MethodologyFeatureDto(String label, boolean value) {}
    record MethodologyDto(String intro, List<MethodologyFeatureDto> features) {}

    /**
     * Computes which activation conditions are not yet met, to help the frontend
     * display "complete these fields to publish your profile".
     */
    private static List<String> computeMissingForActivation(Tutor t) {
        List<String> missing = new ArrayList<>();
        if (t.bio() == null || t.bio().isBlank()) missing.add("bio");
        if (t.assignedSubjectIds() == null || t.assignedSubjectIds().isEmpty()) missing.add("subjects");
        if (t.schedules() == null || t.schedules().isEmpty()) missing.add("schedules");
        if (t.hourlyRate() <= 0) missing.add("hourlyRate");
        return List.copyOf(missing);
    }

    static TutorProfileResponse from(Tutor t) {
        return new TutorProfileResponse(
                t.id().value(),
                t.name(),
                t.subjectSpecialty(),
                t.university(),
                t.location(),
                t.modalidad(),
                t.rating(),
                t.reviewsCount(),
                t.bio(),
                t.photoUrl(),
                t.active(),
                t.hourlyRate(),
                t.assignedSubjectIds() == null ? List.of()
                        : t.assignedSubjectIds().stream()
                                .map(s -> new SubjectDto(s.value()))
                                .toList(),
                new MethodologyDto(
                        t.methodology().intro(),
                        t.methodology().features().stream()
                                .map(f -> new MethodologyFeatureDto(f.label(), f.value())).toList()),
                t.schedules().stream().map(s -> new ScheduleDto(s.days(), s.hours())).toList(),
                t.schedulesNote(),
                t.plans().stream().map(p -> new PlanDto(p.name(), p.description(), p.price(), p.unit(), p.badge(), p.featured())).toList(),
                t.phoneNumber(),
                computeMissingForActivation(t)
        );
    }
}
