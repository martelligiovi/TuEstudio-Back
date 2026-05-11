package com.tuestudio.tutor.application.port;

import com.tuestudio.tutor.application.usecase.SearchCriteria;
import com.tuestudio.tutor.application.usecase.TutorSummary;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;
import java.util.List;
import java.util.Optional;

public interface TutorRepositoryPort {
    List<TutorSummary> search(SearchCriteria criteria);
    Optional<Tutor> findById(TutorId id);
    boolean existsById(TutorId id);
    void save(Tutor tutor);
}
