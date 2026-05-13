package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectAlreadyExistsException;
import com.tuestudio.subject.domain.SubjectId;
import org.springframework.transaction.annotation.Transactional;

public class CreateSubjectService implements CreateSubjectUseCase {

    private final SubjectRepositoryPort repo;

    public CreateSubjectService(SubjectRepositoryPort repo) {
        this.repo = repo;
    }

    @Transactional
    public Subject create(String canonicalName) {
        if (repo.existsByCanonicalNameIgnoreCase(canonicalName)) {
            throw new SubjectAlreadyExistsException(canonicalName);
        }
        Subject subject = Subject.create(SubjectId.newId(), canonicalName);
        return repo.save(subject);
    }
}
