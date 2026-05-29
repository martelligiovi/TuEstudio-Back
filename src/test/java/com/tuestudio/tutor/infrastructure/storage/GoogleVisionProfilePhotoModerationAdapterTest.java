package com.tuestudio.tutor.infrastructure.storage;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.Likelihood;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import com.google.rpc.Status;
import com.tuestudio.tutor.application.usecase.ProfilePhotoModerationUnavailableException;
import com.tuestudio.tutor.application.usecase.ProfilePhotoUpload;
import com.tuestudio.tutor.application.usecase.ProfilePhotoValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleVisionProfilePhotoModerationAdapterTest {

    @Mock ImageAnnotatorClient client;

    @Test
    void assertAllowed_rejectsLikelyAdultContent() {
        GoogleVisionProfilePhotoModerationAdapter adapter = adapter(false);
        when(client.batchAnnotateImages(anyList())).thenReturn(response(
                Likelihood.LIKELY,
                Likelihood.UNLIKELY,
                Likelihood.VERY_UNLIKELY
        ));

        assertThatThrownBy(() -> adapter.assertAllowed(upload()))
                .isInstanceOf(ProfilePhotoValidationException.class)
                .hasMessageContaining("adult_content");
    }

    @Test
    void assertAllowed_rejectsVeryLikelyViolence() {
        GoogleVisionProfilePhotoModerationAdapter adapter = adapter(false);
        when(client.batchAnnotateImages(anyList())).thenReturn(response(
                Likelihood.UNLIKELY,
                Likelihood.UNLIKELY,
                Likelihood.VERY_LIKELY
        ));

        assertThatThrownBy(() -> adapter.assertAllowed(upload()))
                .isInstanceOf(ProfilePhotoValidationException.class)
                .hasMessageContaining("violent_content");
    }

    @Test
    void assertAllowed_allowsVeryLikelyRacy_whenRacyRejectionIsDisabled() {
        GoogleVisionProfilePhotoModerationAdapter adapter = adapter(false);
        when(client.batchAnnotateImages(anyList())).thenReturn(response(
                Likelihood.UNLIKELY,
                Likelihood.VERY_LIKELY,
                Likelihood.UNLIKELY
        ));

        assertThatCode(() -> adapter.assertAllowed(upload())).doesNotThrowAnyException();
    }

    @Test
    void assertAllowed_rejectsVeryLikelyRacy_whenRacyRejectionIsEnabled() {
        GoogleVisionProfilePhotoModerationAdapter adapter = adapter(true);
        when(client.batchAnnotateImages(anyList())).thenReturn(response(
                Likelihood.UNLIKELY,
                Likelihood.VERY_LIKELY,
                Likelihood.UNLIKELY
        ));

        assertThatThrownBy(() -> adapter.assertAllowed(upload()))
                .isInstanceOf(ProfilePhotoValidationException.class)
                .hasMessageContaining("racy_content");
    }

    @Test
    void assertAllowed_failsClosedWhenVisionClientThrows() {
        GoogleVisionProfilePhotoModerationAdapter adapter = adapter(false);
        when(client.batchAnnotateImages(anyList())).thenThrow(new IllegalStateException("network"));

        assertThatThrownBy(() -> adapter.assertAllowed(upload()))
                .isInstanceOf(ProfilePhotoModerationUnavailableException.class);
    }

    @Test
    void assertAllowed_failsClosedWhenVisionReturnsError() {
        GoogleVisionProfilePhotoModerationAdapter adapter = adapter(false);
        BatchAnnotateImagesResponse response = BatchAnnotateImagesResponse.newBuilder()
                .addResponses(AnnotateImageResponse.newBuilder()
                        .setError(Status.newBuilder().setMessage("vision unavailable")))
                .build();
        when(client.batchAnnotateImages(anyList())).thenReturn(response);

        assertThatThrownBy(() -> adapter.assertAllowed(upload()))
                .isInstanceOf(ProfilePhotoModerationUnavailableException.class);
    }

    private GoogleVisionProfilePhotoModerationAdapter adapter(boolean rejectRacy) {
        ProfilePhotoModerationProperties properties = new ProfilePhotoModerationProperties(
                true,
                Likelihood.LIKELY,
                Likelihood.VERY_LIKELY,
                rejectRacy,
                Likelihood.VERY_LIKELY
        );
        return new GoogleVisionProfilePhotoModerationAdapter(client, properties);
    }

    private BatchAnnotateImagesResponse response(Likelihood adult, Likelihood racy, Likelihood violence) {
        SafeSearchAnnotation annotation = SafeSearchAnnotation.newBuilder()
                .setAdult(adult)
                .setRacy(racy)
                .setViolence(violence)
                .build();
        return BatchAnnotateImagesResponse.newBuilder()
                .addResponses(AnnotateImageResponse.newBuilder().setSafeSearchAnnotation(annotation))
                .build();
    }

    private ProfilePhotoUpload upload() {
        byte[] content = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        return new ProfilePhotoUpload("ana.jpg", "image/jpeg", content.length, content);
    }
}
