package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.application.usecase.SearchCriteria;
import com.tuestudio.tutor.application.usecase.TutorSummary;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
class TutorJpaAdapter implements TutorRepositoryPort {

    private final TutorJpaRepository repository;

    TutorJpaAdapter(TutorJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorSummary> search(SearchCriteria criteria, Set<UUID> matchingSubjectIds) {
        return repository.findAll(TutorSpecification.from(criteria, matchingSubjectIds))
                .stream()
                .map(e -> {
                    var p = e.toSummary();
                    List<UUID> subjectIds = p.subjectIds().stream().toList();
                    return new TutorSummary(TutorId.of(p.id()), p.name(), p.university(),
                            subjectIds, p.hourlyRate(), p.active(), p.photoUrl());
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tutor> findById(TutorId id) {
        return repository.findById(id.value()).map(TutorJpaEntity::toDomain);
    }

    @Override
    public boolean existsById(TutorId id) {
        return repository.existsById(id.value());
    }

    @Override
    public void save(Tutor tutor) {
        repository.save(TutorJpaEntity.fromDomain(tutor));
    }
}
