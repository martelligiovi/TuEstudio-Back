package com.tuestudio.tutor.application.port;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface SubjectLookupPort {

    /** Returns only the UUIDs from {@code requestedIds} that actually exist in the catalog. */
    Set<UUID> findExistingIds(Set<UUID> requestedIds);

    /** Batch-fetches subject summaries. Unknown ids are silently omitted from the result. */
    List<SubjectSummary> findByIds(Collection<UUID> ids);

    /** Returns the set of catalog subject UUIDs whose canonicalName or alias matches the query text. */
    Set<UUID> findIdsMatching(String queryText);
}
