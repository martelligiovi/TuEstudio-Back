package com.tuestudio.catalog.application.usecase;

import com.tuestudio.catalog.domain.Career;
import com.tuestudio.catalog.domain.CatalogSubject;
import com.tuestudio.catalog.domain.University;
import java.util.List;

public record CatalogData(
        List<University> universities,
        List<Career> careers,
        List<CatalogSubject> subjects
) {}
