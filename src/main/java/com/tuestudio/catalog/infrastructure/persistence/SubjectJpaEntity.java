package com.tuestudio.catalog.infrastructure.persistence;

import com.tuestudio.catalog.domain.CatalogSubject;
import jakarta.persistence.*;

@Entity(name = "CatalogSubjectJpaEntity")
@Table(name = "catalog_subjects")
class SubjectJpaEntity {

    @Id
    private String id;
    private String name;
    private String icon;

    protected SubjectJpaEntity() {}

    SubjectJpaEntity(String id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    CatalogSubject toDomain() { return new CatalogSubject(id, name, icon); }

    String getId() { return id; }
}
