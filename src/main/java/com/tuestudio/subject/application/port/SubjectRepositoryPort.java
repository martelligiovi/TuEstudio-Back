package com.tuestudio.subject.application.port;

import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;

import java.util.List;
import java.util.Optional;

public interface SubjectRepositoryPort {
    Subject save(Subject subject);
    Optional<Subject> findById(SubjectId id);
    List<Subject> findAll();
    boolean existsByCanonicalNameIgnoreCase(String canonicalName);
    List<Subject> searchByQuery(String query);
}
