package com.tuestudio.subject.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class SubjectIdTest {

    @Test
    void of_withNullUuid_throws() {
        assertThatThrownBy(() -> SubjectId.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void of_withUuid_returnsRecord() {
        UUID uuid = UUID.randomUUID();
        SubjectId id = SubjectId.of(uuid);

        assertThat(id.value()).isEqualTo(uuid);
        assertThat(SubjectId.of(uuid)).isEqualTo(id);
    }

    @Test
    void newId_returnsDistinct() {
        SubjectId a = SubjectId.newId();
        SubjectId b = SubjectId.newId();

        assertThat(a).isNotEqualTo(b);
    }
}
