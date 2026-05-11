package com.tuestudio.tutor.infrastructure.provisioning;

import com.tuestudio.auth.application.port.TutorProvisioningPort;
import com.tuestudio.auth.domain.User;
import com.tuestudio.tutor.application.usecase.CreateTutorProfileUseCase;
import com.tuestudio.tutor.domain.TutorId;
import org.springframework.stereotype.Component;

/**
 * Tutor-hexagon adapter that implements the auth-hexagon's TutorProvisioningPort.
 *
 * <p>Cross-hexagon glue (A2 pattern — decision #53):
 * Auth defines the port; tutor infrastructure provides the adapter.</p>
 *
 * <p>This thin adapter translates auth.User → (TutorId, name) and delegates to
 * CreateTutorProfileUseCase. It runs inside the caller's transaction (REQUIRED propagation).</p>
 */
@Component
public final class TutorProvisioningAdapter implements TutorProvisioningPort {

    private final CreateTutorProfileUseCase createTutorProfile;

    public TutorProvisioningAdapter(CreateTutorProfileUseCase createTutorProfile) {
        this.createTutorProfile = createTutorProfile;
    }

    @Override
    public void provisionFor(User user) {
        createTutorProfile.createStub(TutorId.of(user.id()), user.name());
    }
}
