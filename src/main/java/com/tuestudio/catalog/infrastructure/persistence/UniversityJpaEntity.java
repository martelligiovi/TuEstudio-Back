package com.tuestudio.catalog.infrastructure.persistence;

import com.tuestudio.catalog.domain.University;
import jakarta.persistence.*;

@Entity
@Table(name = "catalog_universities")
class UniversityJpaEntity {

    @Id
    private String id;
    private String name;
    private String logo;

    protected UniversityJpaEntity() {}

    UniversityJpaEntity(String id, String name, String logo) {
        this.id = id;
        this.name = name;
        this.logo = logo;
    }

    University toDomain() { return new University(id, name, logo); }

    String getId() { return id; }
}
