package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.tutor.application.port.SubjectLookupPort;
import com.tuestudio.tutor.application.port.SubjectSummary;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing {@link SubjectLookupPort} for the tutor context.
 * Delegates to the subject context's {@link SubjectRepositoryPort} Spring bean.
 * Uses ONLY cross-context primitive methods — no imports from {@code com.tuestudio.subject.domain.*}.
 */
@Component
class SubjectLookupAdapter implements SubjectLookupPort {

    private final SubjectRepositoryPort subjects;

    SubjectLookupAdapter(SubjectRepositoryPort subjects) {
        this.subjects = subjects;
    }

    @Override
    public Set<UUID> findExistingIds(Set<UUID> requestedIds) {
        return subjects.findExistingIds(requestedIds);
    }

    @Override
    public List<SubjectSummary> findByIds(Collection<UUID> ids) {
        Set<UUID> idSet = ids instanceof Set ? (Set<UUID>) ids : Set.copyOf(ids);
        Map<UUID, String> nameMap = subjects.findCanonicalNamesByIds(idSet);
        Map<UUID, String> iconMap = subjects.findIconsByIds(idSet);
        return ids.stream()
                .filter(nameMap::containsKey)
                .map(id -> new SubjectSummary(id, nameMap.get(id), iconMap.get(id)))
                .toList();
    }

    @Override
    public Set<UUID> findIdsMatching(String queryText) {
        return Set.copyOf(subjects.findIdsMatching(queryText));
    }
}
