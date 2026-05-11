package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.application.port.ContactRequestRepositoryPort;
import com.tuestudio.tutor.domain.ContactRequest;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public List<ContactRequest> findByTutorId(UUID tutorId) {
        return repository.findByTutorIdOrderByCreatedAtDesc(tutorId)
                .stream().map(ContactRequestJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<ContactRequest> findById(UUID id) {
        return repository.findById(id).map(ContactRequestJpaEntity::toDomain);
    }
}
