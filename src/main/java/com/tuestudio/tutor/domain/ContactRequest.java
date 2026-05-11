package com.tuestudio.tutor.domain;

import java.time.Instant;
import java.util.UUID;

public final class ContactRequest {
    private final UUID id;
    private final TutorId tutorId;
    private final String nombre;
    private final String telefono;
    private final String universidad;
    private final String carrera;
    private final String materia;
    private final ContactRequestStatus status;
    private final Instant createdAt;

    public ContactRequest(UUID id, TutorId tutorId, String nombre, String telefono,
                          String universidad, String carrera, String materia,
                          ContactRequestStatus status, Instant createdAt) {
        this.id = id;
        this.tutorId = tutorId;
        this.nombre = nombre;
        this.telefono = telefono;
        this.universidad = universidad;
        this.carrera = carrera;
        this.materia = materia;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static ContactRequest create(TutorId tutorId, String nombre, String telefono,
                                        String universidad, String carrera, String materia) {
        return new ContactRequest(UUID.randomUUID(), tutorId, nombre, telefono,
                universidad, carrera, materia, ContactRequestStatus.PENDING, Instant.now());
    }

    public ContactRequest attend() {
        return new ContactRequest(id, tutorId, nombre, telefono, universidad, carrera, materia,
                ContactRequestStatus.ATTENDED, createdAt);
    }

    public UUID id() { return id; }
    public TutorId tutorId() { return tutorId; }
    public String nombre() { return nombre; }
    public String telefono() { return telefono; }
    public String universidad() { return universidad; }
    public String carrera() { return carrera; }
    public String materia() { return materia; }
    public ContactRequestStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
}
