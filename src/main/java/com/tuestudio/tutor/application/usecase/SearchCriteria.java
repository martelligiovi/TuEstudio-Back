package com.tuestudio.tutor.application.usecase;

public record SearchCriteria(
        String universidad,
        String materia,
        String carrera,
        Double minPrice,
        Double maxPrice
) {}
