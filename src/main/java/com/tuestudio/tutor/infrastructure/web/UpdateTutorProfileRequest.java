package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.tutor.application.usecase.UpdateTutorProfileCommand;
import com.tuestudio.tutor.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for PUT /api/teacher/profile.
 * Contains 14 client-editable fields. Id is derived from JWT.
 * rating, reviewsCount, active are server-managed — never accepted from clients.
 */
public record UpdateTutorProfileRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String subjectSpecialty,
        @Size(max = 255) String university,
        @Size(max = 255) String location,
        @Size(max = 255) String modalidad,
        @Size(max = 2000) String bio,
        @Size(max = 255) String photoUrl,
        @PositiveOrZero double hourlyRate,
        @Valid List<SubjectDto> subjects,
        @Valid MethodologyDto methodology,
        @Valid List<ScheduleDto> schedules,
        @Size(max = 255) String schedulesNote,
        @Valid List<PlanDto> plans,
        @Size(max = 255) String phoneNumber
) {
    public record SubjectDto(String name, String description, String icon) {}
    public record ScheduleDto(String days, String hours) {}
    public record PlanDto(String name, String description, String price, String unit, String badge, boolean featured) {}
    public record MethodologyFeatureDto(String label, boolean value) {}
    public record MethodologyDto(String intro, List<MethodologyFeatureDto> features) {}

    /**
     * Converts this request to the command used by the application layer.
     * Null collections become empty lists (PUT full-replacement semantics).
     */
    public UpdateTutorProfileCommand toCommand(TutorId id) {
        return new UpdateTutorProfileCommand(
                id,
                name,
                subjectSpecialty,
                university,
                location,
                modalidad,
                bio,
                photoUrl,
                hourlyRate,
                subjects == null ? List.of()
                        : subjects.stream()
                        .map(s -> new Subject(s.name(), s.description(), s.icon()))
                        .toList(),
                methodology == null
                        ? new Methodology("", List.of())
                        : new Methodology(
                        methodology.intro(),
                        methodology.features() == null ? List.of()
                                : methodology.features().stream()
                                .map(f -> new MethodologyFeature(f.label(), f.value()))
                                .toList()),
                schedules == null ? List.of()
                        : schedules.stream()
                        .map(s -> new Schedule(s.days(), s.hours()))
                        .toList(),
                schedulesNote,
                plans == null ? List.of()
                        : plans.stream()
                        .map(p -> new Plan(p.name(), p.description(), p.price(), p.unit(), p.badge(), p.featured()))
                        .toList(),
                phoneNumber
        );
    }
}
