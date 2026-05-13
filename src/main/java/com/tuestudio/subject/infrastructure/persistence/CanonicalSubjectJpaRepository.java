package com.tuestudio.subject.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface CanonicalSubjectJpaRepository extends JpaRepository<SubjectJpaEntity, UUID> {

    boolean existsByCanonicalNameIgnoreCase(String canonicalName);

    @Query("SELECT DISTINCT s FROM SubjectJpaEntity s LEFT JOIN s.aliases a "
         + "WHERE LOWER(s.canonicalName) LIKE LOWER(CONCAT('%', :q, '%')) "
         + "OR LOWER(a) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<SubjectJpaEntity> searchByQuery(@Param("q") String q);
}
