package com.tuestudio.tutor.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

public record ContactRequestBody(
        @NotBlank String nombre,
        @NotBlank String telefono,
        @NotBlank String universidad,
        @NotBlank String carrera,
        @NotBlank String materia
) {}
