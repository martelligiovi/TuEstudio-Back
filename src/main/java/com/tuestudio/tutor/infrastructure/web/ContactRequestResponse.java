package com.tuestudio.tutor.infrastructure.web;

import com.tuestudio.tutor.domain.ContactRequest;

public record ContactRequestResponse(
        String id,
        String studentName,
        String studentPhone,
        String university,
        String career,
        String subject,
        String status,
        String createdAt
) {
    static ContactRequestResponse from(ContactRequest cr) {
        return new ContactRequestResponse(
                cr.id().toString(),
                cr.nombre(),
                cr.telefono(),
                cr.universidad(),
                cr.carrera(),
                cr.materia(),
                cr.status().name(),
                cr.createdAt().toString()
        );
    }
}
