package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.ContactRequestRepositoryPort;
import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.ContactRequest;
import com.tuestudio.tutor.domain.TutorNotFoundException;

public final class RequestContactService implements RequestContactUseCase {

    private final TutorRepositoryPort tutorRepository;
    private final ContactRequestRepositoryPort contactRequestRepository;

    public RequestContactService(TutorRepositoryPort tutorRepository,
                                  ContactRequestRepositoryPort contactRequestRepository) {
        this.tutorRepository = tutorRepository;
        this.contactRequestRepository = contactRequestRepository;
    }

    @Override
    public void request(ContactRequestCommand command) {
        if (!tutorRepository.existsById(command.tutorId())) {
            throw new TutorNotFoundException(command.tutorId());
        }
        ContactRequest req = ContactRequest.create(command.tutorId(), command.nombre(), command.telefono());
        contactRequestRepository.save(req);
    }
}
