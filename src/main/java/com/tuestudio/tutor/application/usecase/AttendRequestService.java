package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.ContactRequestRepositoryPort;
import com.tuestudio.tutor.domain.ContactRequest;
import com.tuestudio.tutor.domain.ContactRequestNotFoundException;
import java.util.UUID;

public final class AttendRequestService implements AttendRequestUseCase {

    private final ContactRequestRepositoryPort repository;

    public AttendRequestService(ContactRequestRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public ContactRequest attend(UUID requestId, UUID tutorId) {
        ContactRequest request = repository.findById(requestId)
                .filter(r -> r.tutorId().value().equals(tutorId))
                .orElseThrow(() -> new ContactRequestNotFoundException(requestId));
        ContactRequest attended = request.attend();
        repository.save(attended);
        return attended;
    }
}
