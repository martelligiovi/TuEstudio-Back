package com.tuestudio.tutor.application.usecase;

import com.tuestudio.tutor.application.port.ProfilePhotoModerationPort;
import com.tuestudio.tutor.application.port.ProfilePhotoStoragePort;
import com.tuestudio.tutor.application.port.TutorRepositoryPort;
import com.tuestudio.tutor.domain.Tutor;
import com.tuestudio.tutor.domain.TutorId;
import com.tuestudio.tutor.domain.TutorNotFoundException;
import jakarta.transaction.Transactional;

import java.util.Locale;
import java.util.Set;

public class UpdateTutorProfilePhotoService implements UpdateTutorProfilePhotoUseCase {

    private static final long MAX_BYTES = 2L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final TutorRepositoryPort repository;
    private final ProfilePhotoStoragePort storage;
    private final ProfilePhotoModerationPort moderation;

    public UpdateTutorProfilePhotoService(TutorRepositoryPort repository,
                                          ProfilePhotoStoragePort storage,
                                          ProfilePhotoModerationPort moderation) {
        this.repository = repository;
        this.storage = storage;
        this.moderation = moderation;
    }

    @Override
    @Transactional
    public Tutor updatePhoto(TutorId id, ProfilePhotoUpload upload) {
        Tutor existing = repository.findById(id)
                .orElseThrow(() -> new TutorNotFoundException(id));

        validate(upload);
        moderation.assertAllowed(upload);

        String photoUrl = storage.store(id, upload);
        Tutor updated = existing.withPhotoUrl(photoUrl);
        repository.save(updated);
        return updated;
    }

    private void validate(ProfilePhotoUpload upload) {
        if (upload == null) {
            throw new ProfilePhotoValidationException("missing_file", "Profile photo file is required");
        }
        if (upload.size() <= 0 || upload.content().length == 0) {
            throw new ProfilePhotoValidationException("empty_file", "Profile photo file is empty");
        }
        if (upload.size() > MAX_BYTES || upload.content().length > MAX_BYTES) {
            throw new ProfilePhotoValidationException("file_too_large", "Profile photo must be 2MB or smaller");
        }
        String contentType = upload.contentType().toLowerCase(Locale.ROOT).trim();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ProfilePhotoValidationException(
                    "unsupported_type",
                    "Only jpg, png or webp profile photos are allowed");
        }
        if (!contentMatchesType(contentType, upload.content())) {
            throw new ProfilePhotoValidationException(
                    "content_type_mismatch",
                    "Profile photo content does not match its declared type");
        }
    }

    private boolean contentMatchesType(String contentType, byte[] content) {
        return switch (contentType) {
            case "image/jpeg" -> content.length >= 3
                    && unsigned(content[0]) == 0xFF
                    && unsigned(content[1]) == 0xD8
                    && unsigned(content[2]) == 0xFF;
            case "image/png" -> content.length >= 8
                    && unsigned(content[0]) == 0x89
                    && content[1] == 0x50
                    && content[2] == 0x4E
                    && content[3] == 0x47
                    && content[4] == 0x0D
                    && content[5] == 0x0A
                    && content[6] == 0x1A
                    && content[7] == 0x0A;
            case "image/webp" -> content.length >= 12
                    && content[0] == 0x52
                    && content[1] == 0x49
                    && content[2] == 0x46
                    && content[3] == 0x46
                    && content[8] == 0x57
                    && content[9] == 0x45
                    && content[10] == 0x42
                    && content[11] == 0x50;
            default -> false;
        };
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }
}
