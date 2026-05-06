package com.tuestudio.catalog.application.port;

import com.tuestudio.catalog.domain.Career;
import java.util.List;

public interface CareerRepositoryPort {
    List<Career> findAll();
}
