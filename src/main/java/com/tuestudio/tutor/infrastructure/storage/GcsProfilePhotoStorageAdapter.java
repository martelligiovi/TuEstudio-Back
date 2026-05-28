package com.tuestudio.tutor.infrastructure.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.tuestudio.tutor.application.port.ProfilePhotoStoragePort;
import com.tuestudio.tutor.application.usecase.ProfilePhotoUpload;
import com.tuestudio.tutor.domain.TutorId;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class GcsProfilePhotoStorageAdapter implements ProfilePhotoStoragePort {

    private final Storage storage;
    private final GcsProfilePhotoStorageProperties properties;

    public GcsProfilePhotoStorageAdapter(Storage storage, GcsProfilePhotoStorageProperties properties) {
        this.storage = storage;
        this.properties = properties;
    }

    @Override
    public String store(TutorId tutorId, ProfilePhotoUpload upload) {
        String objectName = objectName(tutorId, upload.contentType());
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(properties.bucketName(), objectName))
                .setContentType(upload.contentType())
                .setCacheControl("public, max-age=31536000, immutable")
                .build();

        storage.create(blobInfo, upload.content());
        return properties.publicBaseUrl() + "/" + objectName;
    }

    private String objectName(TutorId tutorId, String contentType) {
        return properties.objectPrefix()
                + "/" + tutorId.value()
                + "/" + UUID.randomUUID()
                + "." + extension(contentType);
    }

    private String extension(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT).trim()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new IllegalArgumentException("Unsupported profile photo content type: " + contentType);
        };
    }
}
