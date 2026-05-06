package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.domain.TutorId;

public record ContactRequestCommand(TutorId tutorId, String nombre, String telefono) {}
