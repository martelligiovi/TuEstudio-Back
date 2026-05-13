package com.tuestudio.auth.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RoleTest {

    @Test
    void values_containsAdmin() {
        assertThat(Role.values()).contains(Role.ADMIN);
    }

    @Test
    void valueOf_ADMIN_returnsEnum() {
        assertThat(Role.valueOf("ADMIN")).isEqualTo(Role.ADMIN);
    }

    @Test
    void existing_STUDENT_TEACHER_unchanged() {
        assertThat(Role.values()).contains(Role.STUDENT, Role.TEACHER);
    }
}
