package com.tuestudio.catalog.application.usecase;

import com.tuestudio.catalog.application.port.CareerRepositoryPort;
import com.tuestudio.catalog.application.port.SubjectRepositoryPort;
import com.tuestudio.catalog.application.port.UniversityRepositoryPort;

public final class GetCatalogService implements GetCatalogUseCase {

    private final UniversityRepositoryPort universityRepository;
    private final CareerRepositoryPort careerRepository;
    private final SubjectRepositoryPort subjectRepository;

    public GetCatalogService(UniversityRepositoryPort universityRepository,
                              CareerRepositoryPort careerRepository,
                              SubjectRepositoryPort subjectRepository) {
        this.universityRepository = universityRepository;
        this.careerRepository = careerRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public CatalogData getCatalog() {
        return new CatalogData(
                universityRepository.findAll(),
                careerRepository.findAll(),
                subjectRepository.findAll()
        );
    }
}
