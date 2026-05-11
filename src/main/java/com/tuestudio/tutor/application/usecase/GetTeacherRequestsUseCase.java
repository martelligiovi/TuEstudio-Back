package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.domain.ContactRequest;
import java.util.List;
import java.util.UUID;

public interface GetTeacherRequestsUseCase {
    List<ContactRequest> getByTutorId(UUID tutorId);
}
