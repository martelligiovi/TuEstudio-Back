package com.tuestudio.auth.application.port;

import com.tuestudio.auth.domain.User;

/**
 * Outbound port: provisions a Tutor stub for a newly registered TEACHER user.
 * Defined in the auth application layer (A2 pattern — decision #53).
 * The tutor hexagon provides the sole implementation via TutorProvisioningAdapter.
 *
 * <p>MUST only be called when user.role() == Role.TEACHER.</p>
 * <p>Runs inside the caller's transaction (REQUIRED propagation).</p>
 */
public interface TutorProvisioningPort {
    /**
     * Provisions a tutor stub for the given TEACHER user.
     *
     * @param user the newly created TEACHER user — must not be null
     */
    void provisionFor(User user);
}
