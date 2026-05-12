package com.tuestudio.tutor.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TutorTest {

    // -------------------------------------------------------------------------
    // Task 1.1 — Tutor.stub factory
    // -------------------------------------------------------------------------

    @Test
    void stub_setsIdAndName_activeFalse() {
        TutorId id = TutorId.of(UUID.randomUUID());
        Tutor t = Tutor.stub(id, "Ana García");

        assertThat(t.id()).isEqualTo(id);
        assertThat(t.name()).isEqualTo("Ana García");
        assertThat(t.active()).isFalse();
    }

    @Test
    void stub_defaultsNumericFieldsToZero() {
        TutorId id = TutorId.of(UUID.randomUUID());
        Tutor t = Tutor.stub(id, "Ana");

        assertThat(t.hourlyRate()).isEqualTo(0.0);
        assertThat(t.rating()).isEqualTo(0.0);
        assertThat(t.reviewsCount()).isEqualTo(0);
    }

    @Test
    void stub_defaultsCollectionsToEmpty() {
        TutorId id = TutorId.of(UUID.randomUUID());
        Tutor t = Tutor.stub(id, "Ana");

        assertThat(t.subjects()).isEmpty();
        assertThat(t.schedules()).isEmpty();
        assertThat(t.plans()).isEmpty();
    }

    @Test
    void stub_defaultsStringFieldsToNullOrBlank() {
        TutorId id = TutorId.of(UUID.randomUUID());
        Tutor t = Tutor.stub(id, "Ana");

        assertThat(t.bio()).isNullOrEmpty();
        assertThat(t.photoUrl()).isNull();
        assertThat(t.location()).isNull();
        assertThat(t.university()).isNull();
        assertThat(t.phoneNumber()).isNull();
        assertThat(t.subjectSpecialty()).isNull();
    }

    @Test
    void stub_methodologyIsNonNull() {
        TutorId id = TutorId.of(UUID.randomUUID());
        Tutor t = Tutor.stub(id, "Ana");

        assertThat(t.methodology()).isNotNull();
    }

    @Test
    void stub_throwsOnNullId() {
        assertThatThrownBy(() -> Tutor.stub(null, "Ana"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void stub_throwsOnBlankName() {
        TutorId id = TutorId.of(UUID.randomUUID());
        assertThatThrownBy(() -> Tutor.stub(id, "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // -------------------------------------------------------------------------
    // Task 1.2 — isMinimallyComplete + recomputeActive
    // -------------------------------------------------------------------------

    @Test
    void isMinimallyComplete_allConditionsMet_returnsTrue() {
        Tutor t = completeStub();
        assertThat(t.isMinimallyComplete()).isTrue();
    }

    @Test
    void isMinimallyComplete_nullBio_returnsFalse() {
        Tutor t = stubWithBio(null);
        assertThat(t.isMinimallyComplete()).isFalse();
    }

    @Test
    void isMinimallyComplete_blankBio_returnsFalse() {
        Tutor t = stubWithBio("   ");
        assertThat(t.isMinimallyComplete()).isFalse();
    }

    @Test
    void isMinimallyComplete_emptySubjects_returnsFalse() {
        Tutor t = completeStubExcept(false, true, true, 100.0);
        assertThat(t.isMinimallyComplete()).isFalse();
    }

    @Test
    void isMinimallyComplete_emptySchedules_returnsFalse() {
        Tutor t = completeStubExcept(true, false, true, 100.0);
        assertThat(t.isMinimallyComplete()).isFalse();
    }

    @Test
    void isMinimallyComplete_zeroHourlyRate_returnsFalse() {
        Tutor t = completeStubExcept(true, true, true, 0.0);
        assertThat(t.isMinimallyComplete()).isFalse();
    }

    @Test
    void isMinimallyComplete_negativeHourlyRate_returnsFalse() {
        Tutor t = completeStubExcept(true, true, true, -1.0);
        assertThat(t.isMinimallyComplete()).isFalse();
    }

    @Test
    void recomputeActive_setsActiveTrue_whenAllConditionsMet() {
        Tutor t = completeStub();
        Tutor result = t.recomputeActive();
        assertThat(result.active()).isTrue();
    }

    @Test
    void recomputeActive_setActiveFalse_whenBioCleared() {
        // Start with a complete tutor (active=true after recompute), then clear bio
        Tutor complete = completeStub().recomputeActive();
        Tutor cleared = new Tutor(
                complete.id(), complete.name(), complete.subjectSpecialty(), complete.university(),
                complete.location(), complete.modalidad(), complete.rating(), complete.reviewsCount(),
                null, complete.photoUrl(), complete.active(), complete.hourlyRate(),
                complete.subjects(), complete.methodology(), complete.schedules(),
                complete.schedulesNote(), complete.plans(), complete.phoneNumber()
        );
        Tutor result = cleared.recomputeActive();
        assertThat(result.active()).isFalse();
    }

    @Test
    void recomputeActive_returnsThis_whenActiveUnchanged() {
        // A stub is born inactive and isMinimallyComplete=false — recompute keeps active=false
        TutorId id = TutorId.of(UUID.randomUUID());
        Tutor stub = Tutor.stub(id, "Ana");
        // active is already false, recompute should keep it false
        Tutor result = stub.recomputeActive();
        assertThat(result).isSameAs(stub);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Tutor completeStub() {
        return new Tutor(
                TutorId.of(UUID.randomUUID()), "Ana", null, null, null, null,
                0.0, 0, "Some bio", null, false, 100.0,
                java.util.List.of(new Subject("Math", null, null)),
                new Methodology("", java.util.List.of()),
                java.util.List.of(new Schedule("Mon", "9-10")),
                null, java.util.List.of(), null
        );
    }

    private Tutor stubWithBio(String bio) {
        return new Tutor(
                TutorId.of(UUID.randomUUID()), "Ana", null, null, null, null,
                0.0, 0, bio, null, false, 100.0,
                java.util.List.of(new Subject("Math", null, null)),
                new Methodology("", java.util.List.of()),
                java.util.List.of(new Schedule("Mon", "9-10")),
                null, java.util.List.of(), null
        );
    }

    private Tutor completeStubExcept(boolean hasSubjects, boolean hasSchedules,
                                      boolean hasBio, double hourlyRate) {
        return new Tutor(
                TutorId.of(UUID.randomUUID()), "Ana", null, null, null, null,
                0.0, 0, hasBio ? "Some bio" : null, null, false, hourlyRate,
                hasSubjects ? java.util.List.of(new Subject("Math", null, null)) : java.util.List.of(),
                new Methodology("", java.util.List.of()),
                hasSchedules ? java.util.List.of(new Schedule("Mon", "9-10")) : java.util.List.of(),
                null, java.util.List.of(), null
        );
    }
}
