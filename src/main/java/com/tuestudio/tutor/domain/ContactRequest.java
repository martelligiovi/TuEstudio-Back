package com.tuestudio.tutor.domain;

import java.util.UUID;

public final class ContactRequest {
    private final UUID id;
    private final TutorId tutorId;
    private final String nombre;
    private final String telefono;
    private final String universidad;
    private final String carrera;
    private final String materia;

    public ContactRequest(UUID id, TutorId tutorId, String nombre, String telefono,
                          String universidad, String carrera, String materia) {
        this.id = id;
        this.tutorId = tutorId;
        this.nombre = nombre;
        this.telefono = telefono;
        this.universidad = universidad;
        this.carrera = carrera;
        this.materia = materia;
    }

    public static ContactRequest create(TutorId tutorId, String nombre, String telefono,
                                        String universidad, String carrera, String materia) {
        return new ContactRequest(UUID.randomUUID(), tutorId, nombre, telefono, universidad, carrera, materia);
    }

    public UUID id() { return id; }
    public TutorId tutorId() { return tutorId; }
    public String nombre() { return nombre; }
    public String telefono() { return telefono; }
    public String universidad() { return universidad; }
    public String carrera() { return carrera; }
    public String materia() { return materia; }
}
