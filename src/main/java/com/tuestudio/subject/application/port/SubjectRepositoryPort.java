package com.tuestudio.subject.application.port;

import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SubjectRepositoryPort {

    // Existing — used internally by subject context
    Subject save(Subject subject);
    Optional<Subject> findById(SubjectId id);
    List<Subject> findAll();
    boolean existsByCanonicalNameIgnoreCase(String canonicalName);
    List<Subject> searchByQuery(String query);

    // Cross-context query — return only primitives / java.util types (no Subject/SubjectId)

    /** Returns the subset of {@code ids} that actually exist in the catalog. */
    Set<UUID> findExistingIds(Set<UUID> ids);

    /** Map id -> canonicalName for batch enrichment. Unknown ids are absent from the result. */
    Map<UUID, String> findCanonicalNamesByIds(Collection<UUID> ids);

    /** UUIDs of subjects whose canonicalName or any alias matches the query (same semantics as searchByQuery). */
    List<UUID> findIdsMatching(String query);

    /** Map canonicalName -> id for every subject; useful for seeder name resolution. */
    Map<String, UUID> findAllCanonicalNameToIdMap();
}
