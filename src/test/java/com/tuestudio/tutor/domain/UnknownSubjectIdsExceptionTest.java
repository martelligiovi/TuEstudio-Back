package com.tuestudio.tutor.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UnknownSubjectIdsExceptionTest {

    @Test
    void unknown_returnsDefensivelyCopiedSet() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Set<UUID> input = new java.util.HashSet<>(Set.of(id1, id2));

        UnknownSubjectIdsException ex = new UnknownSubjectIdsException(input);

        // Mutate the original input — exception must be unaffected
        input.clear();

        assertThat(ex.unknown()).containsExactlyInAnyOrder(id1, id2);
    }

    @Test
    void unknown_returnsUnmodifiableView() {
        UUID id = UUID.randomUUID();
        UnknownSubjectIdsException ex = new UnknownSubjectIdsException(Set.of(id));

        assertThatThrownBy(() -> ex.unknown().add(UUID.randomUUID()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void constructor_preservesAllIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        UnknownSubjectIdsException ex = new UnknownSubjectIdsException(Set.of(id1, id2, id3));

        assertThat(ex.unknown()).containsExactlyInAnyOrder(id1, id2, id3);
    }
}
