package com.tuestudio.tutor.infrastructure.persistence;

import com.tuestudio.tutor.domain.ContactRequest;
import com.tuestudio.tutor.domain.TutorId;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "contact_requests")
class ContactRequestJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID tutorId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String telefono;

    protected ContactRequestJpaEntity() {}

    static ContactRequestJpaEntity fromDomain(ContactRequest cr) {
        ContactRequestJpaEntity e = new ContactRequestJpaEntity();
        e.id = cr.id();
        e.tutorId = cr.tutorId().value();
        e.nombre = cr.nombre();
        e.telefono = cr.telefono();
        return e;
    }

    ContactRequest toDomain() {
        return new ContactRequest(id, TutorId.of(tutorId), nombre, telefono);
    }
}
