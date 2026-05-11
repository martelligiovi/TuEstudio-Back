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

    private String universidad;
    private String carrera;
    private String materia;

    protected ContactRequestJpaEntity() {}

    static ContactRequestJpaEntity fromDomain(ContactRequest cr) {
        ContactRequestJpaEntity e = new ContactRequestJpaEntity();
        e.id = cr.id();
        e.tutorId = cr.tutorId().value();
        e.nombre = cr.nombre();
        e.telefono = cr.telefono();
        e.universidad = cr.universidad();
        e.carrera = cr.carrera();
        e.materia = cr.materia();
        return e;
    }

    ContactRequest toDomain() {
        return new ContactRequest(id, TutorId.of(tutorId), nombre, telefono, universidad, carrera, materia);
    }
}
