package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.domain.*;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "tutors")
class TutorJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String subjectSpecialty;
    private String university;
    private String location;
    private String modalidad;
    private double rating;
    private int reviewsCount;

    @Column(length = 2000)
    private String bio;

    private String photoUrl;
    private boolean active;
    private double hourlyRate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "tutor_subject_ids",
            joinColumns = @JoinColumn(name = "tutor_id",
                    foreignKey = @ForeignKey(name = "fk_tsi_tutor")))
    @Column(name = "subject_id", nullable = false)
    private Set<UUID> subjectIds = new LinkedHashSet<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "intro", column = @Column(name = "methodology_intro", length = 1000))
    })
    private MethodologyEmbeddable methodology;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "tutor_schedules", joinColumns = @JoinColumn(name = "tutor_id"))
    private List<ScheduleEmbeddable> schedules = new ArrayList<>();

    private String schedulesNote;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "tutor_plans", joinColumns = @JoinColumn(name = "tutor_id"))
    private List<PlanEmbeddable> plans = new ArrayList<>();

    private String phoneNumber;

    protected TutorJpaEntity() {}

    static TutorJpaEntity fromDomain(Tutor t) {
        TutorJpaEntity e = new TutorJpaEntity();
        e.id = t.id().value();
        e.name = t.name();
        e.subjectSpecialty = t.subjectSpecialty();
        e.university = t.university();
        e.location = t.location();
        e.modalidad = t.modalidad();
        e.rating = t.rating();
        e.reviewsCount = t.reviewsCount();
        e.bio = t.bio();
        e.photoUrl = t.photoUrl();
        e.active = t.active();
        e.hourlyRate = t.hourlyRate();
        List<AssignedSubjectId> ids = t.assignedSubjectIds();
        if (ids != null) {
            e.subjectIds = new LinkedHashSet<>();
            ids.forEach(s -> e.subjectIds.add(s.value()));
        }
        e.methodology = MethodologyEmbeddable.from(t.methodology());
        e.schedules = t.schedules().stream().map(ScheduleEmbeddable::from).toList();
        e.schedulesNote = t.schedulesNote();
        e.plans = t.plans().stream().map(PlanEmbeddable::from).toList();
        e.phoneNumber = t.phoneNumber();
        return e;
    }

    Tutor toDomain() {
        List<AssignedSubjectId> assignedIds = subjectIds == null
                ? List.of()
                : subjectIds.stream().map(AssignedSubjectId::of).toList();
        return new Tutor(
                TutorId.of(id), name, subjectSpecialty, university, location, modalidad,
                rating, reviewsCount, bio, photoUrl, active, hourlyRate,
                assignedIds,
                methodology != null ? methodology.toDomain() : new Methodology("", List.of()),
                schedules.stream().map(ScheduleEmbeddable::toDomain).toList(),
                schedulesNote,
                plans.stream().map(PlanEmbeddable::toDomain).toList(),
                phoneNumber
        );
    }

    TutorSummaryProjection toSummary() {
        List<UUID> ids = subjectIds == null ? List.of() : new ArrayList<>(subjectIds);
        return new TutorSummaryProjection(
                id, name, university,
                ids,
                hourlyRate, active, photoUrl
        );
    }

    record TutorSummaryProjection(UUID id, String name, String university,
                                   List<UUID> subjectIds, double hourlyRate,
                                   boolean active, String photoUrl) {}

    UUID getId() { return id; }
    String getUniversity() { return university; }
    double getHourlyRate() { return hourlyRate; }
}
