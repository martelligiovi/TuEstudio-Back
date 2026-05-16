package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.application.usecase.SearchCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

class TutorSpecification {

    /**
     * Builds a Specification for tutor search.
     *
     * @param criteria          the search criteria
     * @param matchingSubjectIds pre-resolved set of subject UUIDs that match the materia query;
     *                           empty set means no subjects match (use disjunction to return empty);
     *                           null means materia filter is not active.
     */
    static Specification<TutorJpaEntity> from(SearchCriteria criteria, Set<UUID> matchingSubjectIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // active=true is always required — inactive stubs must never appear in public search
            predicates.add(cb.isTrue(root.get("active")));

            if (criteria.universidad() != null && !criteria.universidad().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("university")),
                        "%" + criteria.universidad().toLowerCase() + "%"));
            }

            if (matchingSubjectIds != null) {
                if (matchingSubjectIds.isEmpty()) {
                    // Empty set: guard against IN () SQL error; return no results
                    predicates.add(cb.disjunction());
                } else {
                    Join<TutorJpaEntity, UUID> j = root.join("subjectIds", JoinType.INNER);
                    predicates.add(j.in(matchingSubjectIds));
                    query.distinct(true);
                }
            }

            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("hourlyRate"), criteria.minPrice()));
            }

            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("hourlyRate"), criteria.maxPrice()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
