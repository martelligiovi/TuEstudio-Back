package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;

public interface RenameSubjectUseCase {
    Subject rename(SubjectId id, String newCanonicalName);
}
