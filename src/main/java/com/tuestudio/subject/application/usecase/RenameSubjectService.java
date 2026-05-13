package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectAlreadyExistsException;
import com.tuestudio.subject.domain.SubjectId;
import com.tuestudio.subject.domain.SubjectNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public class RenameSubjectService implements RenameSubjectUseCase {

    private final SubjectRepositoryPort repo;

    public RenameSubjectService(SubjectRepositoryPort repo) {
        this.repo = repo;
    }

    @Transactional
    public Subject rename(SubjectId id, String newCanonicalName) {
        Subject subject = repo.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException(id));

        if (!subject.canonicalName().equalsIgnoreCase(newCanonicalName)
                && repo.existsByCanonicalNameIgnoreCase(newCanonicalName)) {
            throw new SubjectAlreadyExistsException(newCanonicalName);
        }

        subject.rename(newCanonicalName);
        return repo.save(subject);
    }
}
