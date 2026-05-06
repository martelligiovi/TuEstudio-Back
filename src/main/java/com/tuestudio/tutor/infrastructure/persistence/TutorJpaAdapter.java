package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.application.usecase.SearchCriteria;
import com.tuestudio.tutor.application.usecase.TutorSummary;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
class TutorJpaAdapter implements TutorRepositoryPort {

    private final TutorJpaRepository repository;

    TutorJpaAdapter(TutorJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TutorSummary> search(SearchCriteria criteria) {
        return repository.findAll(TutorSpecification.from(criteria))
                .stream()
                .map(e -> {
                    var p = e.toSummary();
                    return new TutorSummary(TutorId.of(p.id()), p.name(), p.university(),
                            p.subjectNames(), p.hourlyRate(), p.active(), p.photoUrl());
                })
                .toList();
    }

    @Override
    public Optional<Tutor> findById(TutorId id) {
        return repository.findById(id.value()).map(TutorJpaEntity::toDomain);
    }

    @Override
    public boolean existsById(TutorId id) {
        return repository.existsById(id.value());
    }
}
