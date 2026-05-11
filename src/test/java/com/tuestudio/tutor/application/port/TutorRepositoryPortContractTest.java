package com.tuestudio.tutor.application.port;

import com.tuestudio.tutor.domain.Tutor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TutorRepositoryPortContractTest {

    @Test
    void port_declaresVoidSave() throws NoSuchMethodException {
        var method = TutorRepositoryPort.class.getMethod("save", Tutor.class);
        assertThat(method.getReturnType()).isEqualTo(void.class);
    }
}
