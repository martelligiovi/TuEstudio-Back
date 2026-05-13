package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.SubjectId;
import com.tuestudio.tutor.application.port.SubjectLookupPort;
import com.tuestudio.tutor.application.port.SubjectSummary;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing {@link SubjectLookupPort} for the tutor context.
 * Delegates to the subject context's {@link SubjectRepositoryPort} Spring bean.
 * NO imports from {@code com.tuestudio.subject.infrastructure.*} — only the application port.
 */
@Component
class SubjectLookupAdapter implements SubjectLookupPort {

    private final SubjectRepositoryPort subjects;

    SubjectLookupAdapter(SubjectRepositoryPort subjects) {
        this.subjects = subjects;
    }

    @Override
    public Set<UUID> findExistingIds(Set<UUID> requestedIds) {
        return requestedIds.stream()
                .filter(id -> subjects.findById(new SubjectId(id)).isPresent())
                .collect(Collectors.toSet());
    }

    @Override
    public List<SubjectSummary> findByIds(Collection<UUID> ids) {
        return ids.stream()
                .map(id -> subjects.findById(new SubjectId(id)))
                .filter(opt -> opt.isPresent())
                .map(opt -> {
                    var s = opt.get();
                    return new SubjectSummary(s.id().value(), s.canonicalName());
                })
                .toList();
    }

    @Override
    public Set<UUID> findIdsMatching(String queryText) {
        return subjects.searchByQuery(queryText).stream()
                .map(s -> s.id().value())
                .collect(Collectors.toSet());
    }
}
