package com.tuestudio.subject.infrastructure.persistence;

import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "subjects")
class SubjectJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "canonical_name", nullable = false, length = 255)
    private String canonicalName;

    @Column(name = "icon", length = 1024)
    private String icon;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "subject_aliases", joinColumns = @JoinColumn(name = "subject_id"))
    @Column(name = "alias", nullable = false, length = 255)
    private Set<String> aliases = new LinkedHashSet<>();

    protected SubjectJpaEntity() {}

    static SubjectJpaEntity fromDomain(Subject s) {
        SubjectJpaEntity e = new SubjectJpaEntity();
        e.id = s.id().value();
        e.canonicalName = s.canonicalName();
        e.icon = s.icon();
        e.aliases = new LinkedHashSet<>(s.aliases());
        return e;
    }

    void updateFrom(Subject s) {
        this.canonicalName = s.canonicalName();
        this.icon = s.icon();
        this.aliases = new LinkedHashSet<>(s.aliases());
    }

    Subject toDomain() {
        return Subject.rehydrate(SubjectId.of(id), canonicalName, new LinkedHashSet<>(aliases), icon);
    }

    UUID getId() { return id; }

    String getCanonicalName() { return canonicalName; }
}
