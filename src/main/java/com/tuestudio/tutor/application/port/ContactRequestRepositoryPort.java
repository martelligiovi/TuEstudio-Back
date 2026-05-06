package com.tuestudio.tutor.application.port;

import com.tuestudio.tutor.domain.ContactRequest;

public interface ContactRequestRepositoryPort {
    void save(ContactRequest contactRequest);
}
