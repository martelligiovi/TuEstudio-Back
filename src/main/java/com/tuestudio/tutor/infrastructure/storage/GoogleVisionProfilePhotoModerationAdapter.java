package com.tuestudio.tutor.infrastructure.storage;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.Likelihood;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import com.google.protobuf.ByteString;
import com.tuestudio.tutor.application.port.ProfilePhotoModerationPort;
import com.tuestudio.tutor.application.usecase.ProfilePhotoModerationUnavailableException;
import com.tuestudio.tutor.application.usecase.ProfilePhotoUpload;
import com.tuestudio.tutor.application.usecase.ProfilePhotoValidationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.storage.profile-photos.moderation", name = "enabled", havingValue = "true")
public class GoogleVisionProfilePhotoModerationAdapter implements ProfilePhotoModerationPort {

    private final ImageAnnotatorClient client;
    private final ProfilePhotoModerationProperties properties;

    public GoogleVisionProfilePhotoModerationAdapter(ImageAnnotatorClient client,
                                                     ProfilePhotoModerationProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void assertAllowed(ProfilePhotoUpload upload) {
        SafeSearchAnnotation annotation = detectSafeSearch(upload.content());

        if (isAtLeast(annotation.getAdult(), properties.adultThreshold())) {
            reject("adult_content");
        }
        if (isAtLeast(annotation.getViolence(), properties.violenceThreshold())) {
            reject("violent_content");
        }
        if (properties.rejectRacy() && isAtLeast(annotation.getRacy(), properties.racyThreshold())) {
            reject("racy_content");
        }
    }

    private SafeSearchAnnotation detectSafeSearch(byte[] content) {
        Image image = Image.newBuilder()
                .setContent(ByteString.copyFrom(content))
                .build();
        Feature feature = Feature.newBuilder()
                .setType(Feature.Type.SAFE_SEARCH_DETECTION)
                .build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .setImage(image)
                .addFeatures(feature)
                .build();

        BatchAnnotateImagesResponse response;
        try {
            response = client.batchAnnotateImages(List.of(request));
        } catch (ApiException | IllegalStateException ex) {
            throw unavailable("Google Cloud Vision moderation request failed", ex);
        }
        if (response.getResponsesCount() == 0) {
            throw unavailable("Google Cloud Vision returned no moderation response", null);
        }

        AnnotateImageResponse first = response.getResponses(0);
        if (first.hasError()) {
            throw unavailable("Google Cloud Vision moderation failed", null);
        }
        if (!first.hasSafeSearchAnnotation()) {
            throw unavailable("Google Cloud Vision returned no SafeSearch annotation", null);
        }
        return first.getSafeSearchAnnotation();
    }

    private boolean isAtLeast(Likelihood actual, Likelihood threshold) {
        return rank(actual) >= rank(threshold);
    }

    private int rank(Likelihood likelihood) {
        return switch (likelihood) {
            case VERY_UNLIKELY -> 1;
            case UNLIKELY -> 2;
            case POSSIBLE -> 3;
            case LIKELY -> 4;
            case VERY_LIKELY -> 5;
            case UNKNOWN, UNRECOGNIZED -> 0;
        };
    }

    private void reject(String reason) {
        throw new ProfilePhotoValidationException(
                "photo_rejected",
                "Profile photo does not meet content policy: " + reason);
    }

    private ProfilePhotoModerationUnavailableException unavailable(String message, Throwable cause) {
        return new ProfilePhotoModerationUnavailableException(message, cause);
    }
}
