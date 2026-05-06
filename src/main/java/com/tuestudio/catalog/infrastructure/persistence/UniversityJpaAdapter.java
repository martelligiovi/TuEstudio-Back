package com.tuestudio.catalog.infrastructure.persistence;

import com.tuestudio.catalog.application.port.UniversityRepositoryPort;
import com.tuestudio.catalog.domain.University;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
class UniversityJpaAdapter implements UniversityRepositoryPort {

    private final UniversityJpaRepository repository;

    UniversityJpaAdapter(UniversityJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<University> findAll() {
        return repository.findAll().stream().map(UniversityJpaEntity::toDomain).toList();
    }
}
