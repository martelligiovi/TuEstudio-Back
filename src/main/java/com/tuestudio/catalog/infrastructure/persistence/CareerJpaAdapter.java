package com.tuestudio.catalog.infrastructure.persistence;

import com.tuestudio.catalog.application.port.CareerRepositoryPort;
import com.tuestudio.catalog.domain.Career;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
class CareerJpaAdapter implements CareerRepositoryPort {

    private final CareerJpaRepository repository;

    CareerJpaAdapter(CareerJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Career> findAll() {
        return repository.findAll().stream().map(CareerJpaEntity::toDomain).toList();
    }
}
