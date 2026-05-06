package com.tuestudio.tutor.application.usecase;

import java.util.List;

public interface SearchTutorsUseCase {
    List<TutorSummary> search(SearchCriteria criteria);
}
