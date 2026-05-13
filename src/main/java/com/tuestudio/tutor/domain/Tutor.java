package com.tuestudio.tutor.domain;

import java.util.List;
import java.util.Objects;

public final class Tutor {
    private final TutorId id;
    private final String name;
    private final String subjectSpecialty;
    private final String university;
    private final String location;
    private final String modalidad;
    private final double rating;
    private final int reviewsCount;
    private final String bio;
    private final String photoUrl;
    private final boolean active;
    private final double hourlyRate;
    private final List<AssignedSubjectId> assignedSubjectIds;
    private final Methodology methodology;
    private final List<Schedule> schedules;
    private final String schedulesNote;
    private final List<Plan> plans;
    private final String phoneNumber;

    public Tutor(TutorId id, String name, String subjectSpecialty, String university,
                 String location, String modalidad, double rating, int reviewsCount,
                 String bio, String photoUrl, boolean active, double hourlyRate,
                 List<AssignedSubjectId> assignedSubjectIds, Methodology methodology,
                 List<Schedule> schedules, String schedulesNote, List<Plan> plans,
                 String phoneNumber) {
        this.id = id;
        this.name = name;
        this.subjectSpecialty = subjectSpecialty;
        this.university = university;
        this.location = location;
        this.modalidad = modalidad;
        this.rating = rating;
        this.reviewsCount = reviewsCount;
        this.bio = bio;
        this.photoUrl = photoUrl;
        this.active = active;
        this.hourlyRate = hourlyRate;
        this.assignedSubjectIds = assignedSubjectIds;
        this.methodology = methodology;
        this.schedules = schedules;
        this.schedulesNote = schedulesNote;
        this.plans = plans;
        this.phoneNumber = phoneNumber;
    }

    // -------------------------------------------------------------------------
    // Static factories
    // -------------------------------------------------------------------------

    /**
     * Creates an inactive Tutor stub with only id and name populated.
     * Used during provisioning when a TEACHER user registers.
     */
    public static Tutor stub(TutorId id, String name) {
        Objects.requireNonNull(id, "id must not be null");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return new Tutor(
                id, name,
                null,  // subjectSpecialty
                null,  // university
                null,  // location
                null,  // modalidad
                0.0,   // rating
                0,     // reviewsCount
                null,  // bio
                null,  // photoUrl
                false, // active — stubs are INACTIVE by definition
                0.0,   // hourlyRate
                List.of(),                          // assignedSubjectIds
                new Methodology("", List.of()),     // methodology — non-null invariant
                List.of(),                          // schedules
                null,  // schedulesNote
                List.of(),                          // plans
                null   // phoneNumber
        );
    }

    // -------------------------------------------------------------------------
    // Domain logic — active flag auto-flip rule (decision #55)
    // -------------------------------------------------------------------------

    /**
     * Returns true when all four completeness conditions hold simultaneously:
     * bio non-blank, at least 1 assignedSubjectId, at least 1 schedule, hourlyRate > 0.
     */
    public boolean isMinimallyComplete() {
        return bio != null && !bio.isBlank()
                && assignedSubjectIds != null && !assignedSubjectIds.isEmpty()
                && schedules != null && !schedules.isEmpty()
                && hourlyRate > 0.0;
    }

    /**
     * Returns a new Tutor with the active flag set per isMinimallyComplete().
     * Returns the same instance (this) if the flag value would not change.
     */
    public Tutor recomputeActive() {
        boolean newActive = isMinimallyComplete();
        if (newActive == this.active) return this;
        return new Tutor(
                id, name, subjectSpecialty, university, location, modalidad,
                rating, reviewsCount, bio, photoUrl, newActive, hourlyRate,
                assignedSubjectIds, methodology, schedules, schedulesNote, plans, phoneNumber
        );
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public TutorId id() { return id; }
    public String name() { return name; }
    public String subjectSpecialty() { return subjectSpecialty; }
    public String university() { return university; }
    public String location() { return location; }
    public String modalidad() { return modalidad; }
    public double rating() { return rating; }
    public int reviewsCount() { return reviewsCount; }
    public String bio() { return bio; }
    public String photoUrl() { return photoUrl; }
    public boolean active() { return active; }
    public double hourlyRate() { return hourlyRate; }
    public List<AssignedSubjectId> assignedSubjectIds() { return assignedSubjectIds; }
    public Methodology methodology() { return methodology; }
    public List<Schedule> schedules() { return schedules; }
    public String schedulesNote() { return schedulesNote; }
    public List<Plan> plans() { return plans; }
    public String phoneNumber() { return phoneNumber; }
}
