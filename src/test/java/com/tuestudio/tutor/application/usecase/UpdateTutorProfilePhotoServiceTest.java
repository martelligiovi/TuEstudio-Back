package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.ProfilePhotoModerationPort;
import com.tuestudio.tutor.application.port.ProfilePhotoStoragePort;
import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.Methodology;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;
import com.tuestudio.tutor.domain.TutorNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTutorProfilePhotoServiceTest {

    @Mock TutorRepositoryPort repository;
    @Mock ProfilePhotoStoragePort storage;
    @Mock ProfilePhotoModerationPort moderation;

    UpdateTutorProfilePhotoService service;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final TutorId TUTOR_ID = TutorId.of(USER_ID);

    @BeforeEach
    void setUp() {
        service = new UpdateTutorProfilePhotoService(repository, storage, moderation);
    }

    @Test
    void updatePhoto_uploadsPhoto_persistsReturnedUrl_andReturnsUpdatedTutor() {
        Tutor existing = existingTutor(null);
        ProfilePhotoUpload upload = upload("ana.webp", "image/webp", validWebp());
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existing));
        when(storage.store(TUTOR_ID, upload)).thenReturn("https://cdn.tuestudio.com/profile-photos/ana.webp");

        Tutor result = service.updatePhoto(TUTOR_ID, upload);

        verify(moderation).assertAllowed(upload);
        verify(storage).store(TUTOR_ID, upload);
        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().photoUrl()).isEqualTo("https://cdn.tuestudio.com/profile-photos/ana.webp");
        assertThat(result.photoUrl()).isEqualTo("https://cdn.tuestudio.com/profile-photos/ana.webp");
    }

    @Test
    void updatePhoto_preservesExistingProfileFields() {
        Tutor existing = existingTutor("https://old.example/photo.jpg");
        ProfilePhotoUpload upload = upload("ana.jpg", "image/jpeg", validJpeg());
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existing));
        when(storage.store(TUTOR_ID, upload)).thenReturn("https://cdn.tuestudio.com/new.jpg");

        service.updatePhoto(TUTOR_ID, upload);

        ArgumentCaptor<Tutor> captor = ArgumentCaptor.forClass(Tutor.class);
        verify(repository).save(captor.capture());
        Tutor saved = captor.getValue();
        assertThat(saved.name()).isEqualTo(existing.name());
        assertThat(saved.bio()).isEqualTo(existing.bio());
        assertThat(saved.rating()).isEqualTo(existing.rating());
        assertThat(saved.reviewsCount()).isEqualTo(existing.reviewsCount());
        assertThat(saved.photoUrl()).isEqualTo("https://cdn.tuestudio.com/new.jpg");
    }

    @Test
    void updatePhoto_rejectsEmptyFile_withoutStorageCall() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor(null)));
        ProfilePhotoUpload empty = new ProfilePhotoUpload("empty.png", "image/png", 0, new byte[0]);

        assertThatThrownBy(() -> service.updatePhoto(TUTOR_ID, empty))
                .isInstanceOf(ProfilePhotoValidationException.class)
                .hasMessageContaining("empty");

        verify(storage, never()).store(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void updatePhoto_rejectsUnsupportedContentType_withoutStorageCall() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor(null)));
        ProfilePhotoUpload pdf = upload("cv.pdf", "application/pdf", new byte[] {0x25, 0x50, 0x44, 0x46});

        assertThatThrownBy(() -> service.updatePhoto(TUTOR_ID, pdf))
                .isInstanceOf(ProfilePhotoValidationException.class)
                .hasMessageContaining("jpg, png or webp");

        verify(storage, never()).store(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void updatePhoto_rejectsSpoofedContentType_withoutStorageCall() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor(null)));
        ProfilePhotoUpload upload = new ProfilePhotoUpload("spoofed.png", "image/png", 4, new byte[] {0x25, 0x50, 0x44, 0x46});

        assertThatThrownBy(() -> service.updatePhoto(TUTOR_ID, upload))
                .isInstanceOf(ProfilePhotoValidationException.class)
                .hasMessageContaining("does not match");

        verify(storage, never()).store(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void updatePhoto_rejectsFilesOverTwoMegabytes_withoutStorageCall() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor(null)));
        byte[] tooLarge = new byte[(2 * 1024 * 1024) + 1];
        ProfilePhotoUpload upload = new ProfilePhotoUpload("big.jpg", "image/jpeg", tooLarge.length, tooLarge);

        assertThatThrownBy(() -> service.updatePhoto(TUTOR_ID, upload))
                .isInstanceOf(ProfilePhotoValidationException.class)
                .hasMessageContaining("2MB");

        verify(storage, never()).store(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void updatePhoto_rejectsModerationFailure_withoutStorageCall() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.of(existingTutor(null)));
        ProfilePhotoUpload upload = upload("blocked.jpg", "image/jpeg", validJpeg());
        doThrow(new ProfilePhotoValidationException("photo_rejected", "Profile photo does not meet content policy"))
                .when(moderation).assertAllowed(upload);

        assertThatThrownBy(() -> service.updatePhoto(TUTOR_ID, upload))
                .isInstanceOf(ProfilePhotoValidationException.class)
                .hasMessageContaining("content policy");

        verify(storage, never()).store(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void updatePhoto_throwsTutorNotFound_whenTutorDoesNotExist() {
        when(repository.findById(TUTOR_ID)).thenReturn(Optional.empty());
        ProfilePhotoUpload upload = upload("ana.png", "image/png", validPng());

        assertThatThrownBy(() -> service.updatePhoto(TUTOR_ID, upload))
                .isInstanceOf(TutorNotFoundException.class);

        verify(storage, never()).store(any(), any());
        verify(repository, never()).save(any());
    }

    private ProfilePhotoUpload upload(String filename, String contentType, byte[] bytes) {
        return new ProfilePhotoUpload(filename, contentType, bytes.length, bytes);
    }

    private byte[] validJpeg() {
        return new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
    }

    private byte[] validPng() {
        return new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }

    private byte[] validWebp() {
        return new byte[] {0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00, 0x57, 0x45, 0x42, 0x50};
    }

    private Tutor existingTutor(String photoUrl) {
        return new Tutor(TUTOR_ID, "Ana", "Matemáticas", "UBA", "Buenos Aires", "Online",
                4.5, 10, "Bio completa", photoUrl, true, 1500.0,
                List.of(), new Methodology("Método activo", List.of()), List.of(), null, List.of(), "+5491112345678");
    }
}
