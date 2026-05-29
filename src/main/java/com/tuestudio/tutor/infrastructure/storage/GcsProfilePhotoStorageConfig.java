package com.tuestudio.tutor.infrastructure.storage;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties({GcsProfilePhotoStorageProperties.class, ProfilePhotoModerationProperties.class})
public class GcsProfilePhotoStorageConfig {

    @Bean
    public Storage googleCloudStorage() {
        return StorageOptions.getDefaultInstance().getService();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "app.storage.profile-photos.moderation", name = "enabled", havingValue = "true")
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        return ImageAnnotatorClient.create();
    }
}
