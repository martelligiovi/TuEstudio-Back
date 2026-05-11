package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.domain.ContactRequest;
import java.util.UUID;

public interface AttendRequestUseCase {
    ContactRequest attend(UUID requestId, UUID tutorId);
}
