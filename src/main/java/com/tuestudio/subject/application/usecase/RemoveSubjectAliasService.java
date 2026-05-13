package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import com.tuestudio.subject.domain.SubjectNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public class RemoveSubjectAliasService implements RemoveSubjectAliasUseCase {

    private final SubjectRepositoryPort repo;

    public RemoveSubjectAliasService(SubjectRepositoryPort repo) {
        this.repo = repo;
    }

    @Transactional
    public Subject removeAlias(SubjectId id, String alias) {
        Subject subject = repo.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException(id));
        subject.removeAlias(alias);
        return repo.save(subject);
    }
}
