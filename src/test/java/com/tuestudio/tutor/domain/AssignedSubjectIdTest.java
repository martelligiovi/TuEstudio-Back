package com.tuestudio.tutor.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AssignedSubjectIdTest {

    @Test
    void constructor_throwsNullPointerException_whenUuidIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new AssignedSubjectId(null));
    }

    @Test
    void of_throwsNullPointerException_whenUuidIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> AssignedSubjectId.of(null));
    }

    @Test
    void twoInstances_withSameUuid_areEqual() {
        UUID uuid = UUID.randomUUID();
        AssignedSubjectId a = new AssignedSubjectId(uuid);
        AssignedSubjectId b = new AssignedSubjectId(uuid);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void twoInstances_withDifferentUuids_areNotEqual() {
        AssignedSubjectId a = new AssignedSubjectId(UUID.randomUUID());
        AssignedSubjectId b = new AssignedSubjectId(UUID.randomUUID());

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void value_returnsWrappedUuid() {
        UUID uuid = UUID.randomUUID();
        assertThat(new AssignedSubjectId(uuid).value()).isEqualTo(uuid);
    }
}
