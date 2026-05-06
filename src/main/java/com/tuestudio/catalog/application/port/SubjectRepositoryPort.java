package com.tuestudio.catalog.application.port;

import com.tuestudio.catalog.domain.CatalogSubject;
import java.util.List;

public interface SubjectRepositoryPort {
    List<CatalogSubject> findAll();
}
