package com.tuestudio.subject.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class SubjectTest {

    private SubjectId anyId() {
        return SubjectId.of(UUID.randomUUID());
    }

    @Test
    void create_withValidName_returnsSubjectWithEmptyAliases() {
        Subject s = Subject.create(anyId(), "Análisis Matemático");

        assertThat(s.canonicalName()).isEqualTo("Análisis Matemático");
        assertThat(s.aliases()).isEmpty();
    }

    @Test
    void create_withBlankName_throws() {
        assertThatThrownBy(() -> Subject.create(anyId(), "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_withNullName_throws() {
        assertThatThrownBy(() -> Subject.create(anyId(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_withNameOver255_throws() {
        String longName = "A".repeat(256);
        assertThatThrownBy(() -> Subject.create(anyId(), longName))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rename_toValidName_updatesCanonical() {
        Subject s = Subject.create(anyId(), "Álgebra");
        s.rename("Álgebra Lineal");

        assertThat(s.canonicalName()).isEqualTo("Álgebra Lineal");
    }

    @Test
    void rename_toBlankName_throws() {
        Subject s = Subject.create(anyId(), "Física");
        assertThatThrownBy(() -> s.rename("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rename_toExistingAlias_throws() {
        Subject s = Subject.create(anyId(), "Física");
        s.addAlias("Física I");

        assertThatThrownBy(() -> s.rename("física i"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addAlias_withNewValue_addsIt() {
        Subject s = Subject.create(anyId(), "Matemática");
        s.addAlias("Cálculo I");

        assertThat(s.aliases()).contains("Cálculo I");
    }

    @Test
    void addAlias_withDuplicateCaseInsensitive_isIdempotent() {
        Subject s = Subject.create(anyId(), "Matemática");
        s.addAlias("Cálculo I");
        s.addAlias("cálculo i");

        assertThat(s.aliases()).hasSize(1);
    }

    @Test
    void addAlias_collidingWithCanonicalName_throws() {
        Subject s = Subject.create(anyId(), "Física");
        assertThatThrownBy(() -> s.addAlias("física"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void removeAlias_existingCaseInsensitive_removes() {
        Subject s = Subject.create(anyId(), "Química");
        s.addAlias("Química General");
        s.removeAlias("QUÍMICA GENERAL");

        assertThat(s.aliases()).isEmpty();
    }

    @Test
    void removeAlias_missing_isNoOp() {
        Subject s = Subject.create(anyId(), "Química");
        assertThatCode(() -> s.removeAlias("no existe")).doesNotThrowAnyException();
    }

    @Test
    void create_withIcon_storesIcon() {
        Subject s = Subject.create(anyId(), "Álgebra", "🧮");

        assertThat(s.icon()).isEqualTo("🧮");
    }

    @Test
    void create_withNullIcon_iconIsNull() {
        Subject s = Subject.create(anyId(), "Álgebra", null);

        assertThat(s.icon()).isNull();
    }

    @Test
    void create_withBlankIcon_iconIsNull() {
        Subject s = Subject.create(anyId(), "Álgebra", "   ");

        assertThat(s.icon()).isNull();
    }

    @Test
    void changeIcon_setsNewIcon() {
        Subject s = Subject.create(anyId(), "Física");
        s.changeIcon("🍎");

        assertThat(s.icon()).isEqualTo("🍎");
    }

    @Test
    void changeIcon_withBlank_clearsIcon() {
        Subject s = Subject.create(anyId(), "Física", "🍎");
        s.changeIcon("   ");

        assertThat(s.icon()).isNull();
    }

    @Test
    void changeIcon_withNull_clearsIcon() {
        Subject s = Subject.create(anyId(), "Física", "🍎");
        s.changeIcon(null);

        assertThat(s.icon()).isNull();
    }
}
