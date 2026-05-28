package com.tuestudio.tutor.infrastructure.storage;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.tuestudio.tutor.application.usecase.ProfilePhotoUpload;
import com.tuestudio.tutor.domain.TutorId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GcsProfilePhotoStorageAdapterTest {

    @Mock Storage storage;

    @Test
    void store_uploadsToConfiguredBucketWithContentType_andReturnsPublicUrl() {
        GcsProfilePhotoStorageProperties properties = new GcsProfilePhotoStorageProperties(
                "tuestudio-profile-photos",
                "https://cdn.tuestudio.com",
                "profile-photos"
        );
        GcsProfilePhotoStorageAdapter adapter = new GcsProfilePhotoStorageAdapter(storage, properties);
        TutorId tutorId = TutorId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        byte[] content = "fake-webp".getBytes(StandardCharsets.UTF_8);
        ProfilePhotoUpload upload = new ProfilePhotoUpload("ana.webp", "image/webp", content.length, content);

        String publicUrl = adapter.store(tutorId, upload);

        ArgumentCaptor<BlobInfo> blobInfo = ArgumentCaptor.forClass(BlobInfo.class);
        verify(storage).create(blobInfo.capture(), eq(content));
        assertThat(blobInfo.getValue().getBucket()).isEqualTo("tuestudio-profile-photos");
        assertThat(blobInfo.getValue().getName())
                .startsWith("profile-photos/00000000-0000-0000-0000-000000000001/")
                .endsWith(".webp");
        assertThat(blobInfo.getValue().getContentType()).isEqualTo("image/webp");
        assertThat(publicUrl)
                .startsWith("https://cdn.tuestudio.com/profile-photos/00000000-0000-0000-0000-000000000001/")
                .endsWith(".webp");
    }
}
