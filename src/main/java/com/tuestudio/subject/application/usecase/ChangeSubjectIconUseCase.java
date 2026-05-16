package com.tuestudio.subject.application.usecase;

import com.tuestudio.subject.domain.Subject;
import com.tuestudio.subject.domain.SubjectId;

public interface ChangeSubjectIconUseCase {
    Subject changeIcon(SubjectId id, String newIcon);
}
