package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.application.usecase.SearchCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

class TutorSpecification {

    static Specification<TutorJpaEntity> from(SearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.universidad() != null && !criteria.universidad().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("university")),
                        "%" + criteria.universidad().toLowerCase() + "%"));
            }

            if (criteria.materia() != null && !criteria.materia().isBlank()) {
                Join<TutorJpaEntity, SubjectEmbeddable> join = root.join("subjects", JoinType.INNER);
                predicates.add(cb.like(cb.lower(join.get("name")),
                        "%" + criteria.materia().toLowerCase() + "%"));
                query.distinct(true);
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
