package com.tuestudio.tutor.infrastructure.provisioning;

import com.tuestudio.auth.domain.AuthProvider;
import com.tuestudio.auth.domain.HashedPassword;
import com.tuestudio.auth.domain.Role;
import com.tuestudio.auth.domain.User;
import com.tuestudio.tutor.application.usecase.CreateTutorProfileUseCase;
import com.tuestudio.tutor.domain.TutorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorProvisioningAdapterTest {

    @Mock
    CreateTutorProfileUseCase createTutorProfile;

    TutorProvisioningAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TutorProvisioningAdapter(createTutorProfile);
    }

    @Test
    void provisionFor_callsCreateStubWithCorrectTutorId() {
        UUID userId = UUID.randomUUID();
        User user = makeTeacherUser(userId, "Ana");

        when(createTutorProfile.createStub(any(TutorId.class), anyString()))
                .thenReturn(TutorId.of(userId));

        adapter.provisionFor(user);

        ArgumentCaptor<TutorId> idCaptor = ArgumentCaptor.forClass(TutorId.class);
        verify(createTutorProfile).createStub(idCaptor.capture(), anyString());
        assertThat(idCaptor.getValue().value()).isEqualTo(userId);
    }

    @Test
    void provisionFor_passesUserNameToCreateStub() {
        UUID userId = UUID.randomUUID();
        User user = makeTeacherUser(userId, "Carlos");

        when(createTutorProfile.createStub(any(TutorId.class), anyString()))
                .thenReturn(TutorId.of(userId));

        adapter.provisionFor(user);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(createTutorProfile).createStub(any(TutorId.class), nameCaptor.capture());
        assertThat(nameCaptor.getValue()).isEqualTo("Carlos");
    }

    @Test
    void provisionFor_tutor_idDerivedFromUserId() {
        UUID userId = UUID.randomUUID();
        User user = makeTeacherUser(userId, "Ana");

        when(createTutorProfile.createStub(any(TutorId.class), anyString()))
                .thenReturn(TutorId.of(userId));

        adapter.provisionFor(user);

        ArgumentCaptor<TutorId> idCaptor = ArgumentCaptor.forClass(TutorId.class);
        verify(createTutorProfile).createStub(idCaptor.capture(), anyString());
        // The TutorId value must equal the user's id
        assertThat(idCaptor.getValue().value()).isEqualTo(user.id());
    }

    private User makeTeacherUser(UUID id, String name) {
        return new User(id, name, name.toLowerCase() + "@mail.com",
                new HashedPassword("hash"), Role.TEACHER);
    }
}
