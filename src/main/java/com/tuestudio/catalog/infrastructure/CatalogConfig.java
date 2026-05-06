package com.tuestudio.catalog.infrastructure;

import com.tuestudio.catalog.application.port.CareerRepositoryPort;
import com.tuestudio.catalog.application.port.SubjectRepositoryPort;
import com.tuestudio.catalog.application.port.UniversityRepositoryPort;
import com.tuestudio.catalog.application.usecase.GetCatalogService;
import com.tuestudio.catalog.application.usecase.GetCatalogUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogConfig {

    @Bean
    public GetCatalogUseCase getCatalogUseCase(UniversityRepositoryPort universities,
                                                CareerRepositoryPort careers,
                                                SubjectRepositoryPort subjects) {
        return new GetCatalogService(universities, careers, subjects);
    }
}
