package com.tuestudio.tutor.application.port;

import com.tuestudio.tutor.domain.ContactRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRequestRepositoryPort {
    void save(ContactRequest contactRequest);
    List<ContactRequest> findByTutorId(UUID tutorId);
    Optional<ContactRequest> findById(UUID id);
}
