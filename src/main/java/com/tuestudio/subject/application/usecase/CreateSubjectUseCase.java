package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.domain.Subject;

public interface CreateSubjectUseCase {
    Subject create(String canonicalName);
}
