package com.tuestudio.auth.application.usecase;

import com.tuestudio.auth.domain.Role;

public class InvalidRegistrationRoleException extends RuntimeException {
    public InvalidRegistrationRoleException(Role role) {
        super("Public registration is not allowed for role: " + role);
    }
}
