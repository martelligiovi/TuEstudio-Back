package com.tuestudio.tutor.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.profile-photos")
public record GcsProfilePhotoStorageProperties(
        String bucketName,
        String publicBaseUrl,
        String objectPrefix
) {
    public GcsProfilePhotoStorageProperties {
        bucketName = hasText(bucketName) ? bucketName.trim() : "tuestudio-profile-photos";
        publicBaseUrl = stripTrailingSlash(hasText(publicBaseUrl)
                ? publicBaseUrl.trim()
                : "https://storage.googleapis.com/" + bucketName);
        objectPrefix = stripSlashes(hasText(objectPrefix) ? objectPrefix.trim() : "profile-photos");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String stripTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static String stripSlashes(String value) {
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        return stripTrailingSlash(value);
    }
}
