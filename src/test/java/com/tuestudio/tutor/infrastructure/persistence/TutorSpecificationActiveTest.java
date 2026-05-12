package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.application.usecase.SearchCriteria;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorSpecificationActiveTest {

    @Mock Root<TutorJpaEntity> root;
    @Mock CriteriaQuery<?> query;
    @Mock CriteriaBuilder cb;
    @Mock Path<Boolean> activePath;
    @Mock Predicate activePredicate;
    @Mock Predicate andPredicate;

    @BeforeEach
    void setUp() {
        when(root.get("active")).thenReturn((Path) activePath);
        when(cb.isTrue(activePath)).thenReturn(activePredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(andPredicate);
    }

    @Test
    void specification_alwaysIncludesActiveTruePredicate_withNoCriteria() {
        SearchCriteria criteria = new SearchCriteria(null, null, null, null, null);
        Specification<TutorJpaEntity> spec = TutorSpecification.from(criteria);

        spec.toPredicate(root, query, cb);

        verify(cb).isTrue(activePath);
    }

    @Test
    void specification_alwaysIncludesActiveTruePredicate_withUniversityCriteria() {
        when(root.get("university")).thenReturn(mock(Path.class));
        when(cb.lower(any())).thenReturn(mock(Expression.class));
        when(cb.like(any(), anyString())).thenReturn(mock(Predicate.class));

        SearchCriteria criteria = new SearchCriteria("UBA", null, null, null, null);
        Specification<TutorJpaEntity> spec = TutorSpecification.from(criteria);

        spec.toPredicate(root, query, cb);

        // active=true predicate must always be included
        verify(cb).isTrue(activePath);
    }
}
