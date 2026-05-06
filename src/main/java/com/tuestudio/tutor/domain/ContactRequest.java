package com.tuestudio.tutor.domain;

import java.util.UUID;

public final class ContactRequest {
    private final UUID id;
    private final TutorId tutorId;
    private final String nombre;
    private final String telefono;

    public ContactRequest(UUID id, TutorId tutorId, String nombre, String telefono) {
        this.id = id;
        this.tutorId = tutorId;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public static ContactRequest create(TutorId tutorId, String nombre, String telefono) {
        return new ContactRequest(UUID.randomUUID(), tutorId, nombre, telefono);
    }

    public UUID id() { return id; }
    public TutorId tutorId() { return tutorId; }
    public String nombre() { return nombre; }
    public String telefono() { return telefono; }
}
