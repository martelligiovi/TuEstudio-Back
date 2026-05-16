package com.tuestudio.subject.infrastructure.persistence;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("canonicalSubjectJpaAdapter")
public class SubjectJpaAdapter implements SubjectRepositoryPort {

    private final CanonicalSubjectJpaRepository repo;

    public SubjectJpaAdapter(CanonicalSubjectJpaRepository repo) {
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

    // Cross-context query methods — return only primitives / java.util types

    @Override
    public Set<UUID> findExistingIds(Set<UUID> ids) {
        if (ids.isEmpty()) return Set.of();
        return repo.findAllById(ids).stream()
                .map(SubjectJpaEntity::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<UUID, String> findCanonicalNamesByIds(Collection<UUID> ids) {
        if (ids.isEmpty()) return Map.of();
        return repo.findAllById(ids).stream()
                .collect(Collectors.toMap(SubjectJpaEntity::getId, SubjectJpaEntity::getCanonicalName));
    }

    @Override
    public List<UUID> findIdsMatching(String query) {
        if (query == null || query.isBlank()) {
            return repo.findAll().stream().map(SubjectJpaEntity::getId).toList();
        }
        return repo.searchByQuery(query.trim()).stream().map(SubjectJpaEntity::getId).toList();
    }

    @Override
    public Map<String, UUID> findAllCanonicalNameToIdMap() {
        return repo.findAll().stream()
                .collect(Collectors.toMap(SubjectJpaEntity::getCanonicalName, SubjectJpaEntity::getId));
    }

    @Override
    public Map<UUID, String> findIconsByIds(Collection<UUID> ids) {
        if (ids.isEmpty()) return Map.of();
        return repo.findAllById(ids).stream()
                .filter(e -> e.getIcon() != null)
                .collect(Collectors.toMap(SubjectJpaEntity::getId, SubjectJpaEntity::getIcon));
    }
}
