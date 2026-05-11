package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.tutor.domain.*;
import java.util.List;

public record TutorProfileResponse(
        String id,
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
        String phoneNumber
) {
    record SubjectDto(String name, String description, String icon) {}
    record ScheduleDto(String days, String hours) {}
    record PlanDto(String name, String description, String price, String unit, String badge, boolean featured) {}
    record MethodologyFeatureDto(String label, boolean value) {}
    record MethodologyDto(String intro, List<MethodologyFeatureDto> features) {}

    static TutorProfileResponse from(Tutor t) {
        return new TutorProfileResponse(
                t.id().value().toString(), t.name(), t.subjectSpecialty(), t.university(),
                t.location(), t.modalidad(), t.rating(), t.reviewsCount(), t.bio(),
                t.photoUrl(), t.active(), t.hourlyRate(),
                t.subjects().stream().map(s -> new SubjectDto(s.name(), s.description(), s.icon())).toList(),
                new MethodologyDto(
                        t.methodology().intro(),
                        t.methodology().features().stream()
                                .map(f -> new MethodologyFeatureDto(f.label(), f.value())).toList()),
                t.schedules().stream().map(s -> new ScheduleDto(s.days(), s.hours())).toList(),
                t.schedulesNote(),
                t.plans().stream().map(p -> new PlanDto(p.name(), p.description(), p.price(), p.unit(), p.badge(), p.featured())).toList(),
                t.phoneNumber()
        );
    }
}
