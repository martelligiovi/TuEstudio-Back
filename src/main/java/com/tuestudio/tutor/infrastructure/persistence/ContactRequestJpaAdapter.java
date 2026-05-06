package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.application.port.ContactRequestRepositoryPort;
import com.tuestudio.tutor.domain.ContactRequest;
import org.springframework.stereotype.Repository;

@Repository
class ContactRequestJpaAdapter implements ContactRequestRepositoryPort {

    private final ContactRequestJpaRepository repository;

    ContactRequestJpaAdapter(ContactRequestJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(ContactRequest contactRequest) {
        repository.save(ContactRequestJpaEntity.fromDomain(contactRequest));
    }
}
