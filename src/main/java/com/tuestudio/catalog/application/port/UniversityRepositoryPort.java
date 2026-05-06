package com.tuestudio.catalog.application.port;

import com.tuestudio.catalog.domain.University;
import java.util.List;

public interface UniversityRepositoryPort {
    List<University> findAll();
}
