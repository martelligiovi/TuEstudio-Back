package com.tuestudio.tutor.application.usecase;

public record ProfilePhotoUpload(
        String originalFilename,
        String contentType,
        long size,
        byte[] content
) {
    public ProfilePhotoUpload {
        originalFilename = originalFilename == null ? "" : originalFilename;
        contentType = contentType == null ? "" : contentType;
        content = content == null ? new byte[0] : content;
    }
}
