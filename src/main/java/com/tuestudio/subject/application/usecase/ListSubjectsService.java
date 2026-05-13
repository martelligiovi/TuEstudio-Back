package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;

import java.util.List;

public class ListSubjectsService implements ListSubjectsUseCase {

    private final SubjectRepositoryPort repo;

    public ListSubjectsService(SubjectRepositoryPort repo) {
        this.repo = repo;
    }

    public List<Subject> list() {
        return repo.findAll();
    }
}
