package com.tuestudio.catalog.infrastructure.web;

import com.tuestudio.catalog.application.usecase.CatalogData;
import com.tuestudio.catalog.application.usecase.GetCatalogUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final GetCatalogUseCase getCatalog;

    public CatalogController(GetCatalogUseCase getCatalog) {
        this.getCatalog = getCatalog;
    }

    @GetMapping
    public CatalogData catalog() {
        return getCatalog.getCatalog();
    }
}
