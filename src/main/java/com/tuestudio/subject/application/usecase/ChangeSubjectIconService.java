package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;
import com.tuestudio.subject.domain.SubjectNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public class ChangeSubjectIconService implements ChangeSubjectIconUseCase {

    private final SubjectRepositoryPort repo;

    public ChangeSubjectIconService(SubjectRepositoryPort repo) {
        this.repo = repo;
    }

    @Transactional
    public Subject changeIcon(SubjectId id, String newIcon) {
        Subject subject = repo.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException(id));
        subject.changeIcon(newIcon);
        return repo.save(subject);
    }
}
