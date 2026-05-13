package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.application.port.SubjectRepositoryPort;
import com.tuestudio.subject.domain.Subject;

import java.util.List;

public class SearchSubjectsByQueryService implements SearchSubjectsByQueryUseCase {

    private final SubjectRepositoryPort repo;

    public SearchSubjectsByQueryService(SubjectRepositoryPort repo) {
        this.repo = repo;
    }

    public List<Subject> search(String query) {
        if (query == null || query.isBlank()) {
            return repo.findAll();
        }
        return repo.searchByQuery(query.trim());
    }
}
