package com.tuestudio.catalog.infrastructure.persistence;

import com.tuestudio.catalog.application.port.SubjectRepositoryPort;
import com.tuestudio.catalog.domain.CatalogSubject;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
class SubjectJpaAdapter implements SubjectRepositoryPort {

    private final SubjectJpaRepository repository;

    SubjectJpaAdapter(SubjectJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CatalogSubject> findAll() {
        return repository.findAll().stream().map(SubjectJpaEntity::toDomain).toList();
    }
}
