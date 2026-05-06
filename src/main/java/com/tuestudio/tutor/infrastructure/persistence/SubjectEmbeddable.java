package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.domain.Subject;
import jakarta.persistence.Embeddable;

@Embeddable
class SubjectEmbeddable {
    String name;
    String description;
    String icon;

    protected SubjectEmbeddable() {}

    static SubjectEmbeddable from(Subject s) {
        SubjectEmbeddable e = new SubjectEmbeddable();
        e.name = s.name();
        e.description = s.description();
        e.icon = s.icon();
        return e;
    }

    Subject toDomain() { return new Subject(name, description, icon); }
}
