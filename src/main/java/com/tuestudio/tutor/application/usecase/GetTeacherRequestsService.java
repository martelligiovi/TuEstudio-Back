package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.ContactRequestRepositoryPort;
import com.tuestudio.tutor.domain.ContactRequest;
import java.util.List;
import java.util.UUID;

public final class GetTeacherRequestsService implements GetTeacherRequestsUseCase {

    private final ContactRequestRepositoryPort repository;

    public GetTeacherRequestsService(ContactRequestRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<ContactRequest> getByTutorId(UUID tutorId) {
        return repository.findByTutorId(tutorId);
    }
}
