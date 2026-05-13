package com.tuestudio.subject.application.port;

import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class SubjectRepositoryPortContractTest {

    @Test
    void port_isInterface() {
        assertThat(SubjectRepositoryPort.class.isInterface()).isTrue();
    }

    @Test
    void port_hasSaveMethod() throws NoSuchMethodException {
        Method m = SubjectRepositoryPort.class.getMethod("save", Subject.class);
        assertThat(m.getReturnType()).isEqualTo(Subject.class);
    }

    @Test
    void port_hasFindByIdMethod() throws NoSuchMethodException {
        Method m = SubjectRepositoryPort.class.getMethod("findById", SubjectId.class);
        assertThat(m.getReturnType()).isEqualTo(Optional.class);
    }

    @Test
    void port_hasFindAllMethod() throws NoSuchMethodException {
        Method m = SubjectRepositoryPort.class.getMethod("findAll");
        assertThat(m.getReturnType()).isEqualTo(List.class);
    }

    @Test
    void port_hasExistsByCanonicalNameIgnoreCaseMethod() throws NoSuchMethodException {
        Method m = SubjectRepositoryPort.class.getMethod("existsByCanonicalNameIgnoreCase", String.class);
        assertThat(m.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    void port_hasSearchByQueryMethod() throws NoSuchMethodException {
        Method m = SubjectRepositoryPort.class.getMethod("searchByQuery", String.class);
        assertThat(m.getReturnType()).isEqualTo(List.class);
    }
}
