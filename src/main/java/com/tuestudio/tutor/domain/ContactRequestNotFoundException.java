package com.tuestudio.tutor.domain;

import java.util.UUID;

public class ContactRequestNotFoundException extends RuntimeException {
    public ContactRequestNotFoundException(UUID id) {
        super("Contact request not found: " + id);
    }
}
