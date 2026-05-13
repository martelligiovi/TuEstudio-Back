package com.tuestudio.subject.infrastructure.persistence;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("canonicalSubjectJpaAdapter")
public class SubjectJpaAdapter implements SubjectRepositoryPort {

    private final SubjectJpaRepository repo;

    public SubjectJpaAdapter(SubjectJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Subject save(Subject subject) {
        SubjectJpaEntity entity = repo.findById(subject.id().value())
                .map(existing -> { existing.updateFrom(subject); return existing; })
                .orElseGet(() -> SubjectJpaEntity.fromDomain(subject));
        return repo.save(entity).toDomain();
    }

    @Override
    public Optional<Subject> findById(SubjectId id) {
        return repo.findById(id.value()).map(SubjectJpaEntity::toDomain);
    }

    @Override
    public List<Subject> findAll() {
        return repo.findAll().stream().map(SubjectJpaEntity::toDomain).toList();
    }

    @Override
    public boolean existsByCanonicalNameIgnoreCase(String canonicalName) {
        return repo.existsByCanonicalNameIgnoreCase(canonicalName);
    }

    @Override
    public List<Subject> searchByQuery(String query) {
        if (query == null || query.isBlank()) return findAll();
        return repo.searchByQuery(query.trim()).stream().map(SubjectJpaEntity::toDomain).toList();
    }
}
