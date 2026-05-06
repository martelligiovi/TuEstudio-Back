package com.tuestudio.catalog.infrastructure.persistence;

import com.tuestudio.catalog.domain.Career;
import jakarta.persistence.*;

@Entity
@Table(name = "catalog_careers")
class CareerJpaEntity {

    @Id
    private String id;
    private String name;
    private String universityId;

    protected CareerJpaEntity() {}

    CareerJpaEntity(String id, String name, String universityId) {
        this.id = id;
        this.name = name;
        this.universityId = universityId;
    }

    Career toDomain() { return new Career(id, name, universityId); }

    String getId() { return id; }
}
