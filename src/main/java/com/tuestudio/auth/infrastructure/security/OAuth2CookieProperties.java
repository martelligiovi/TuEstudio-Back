package com.tuestudio.auth.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "oauth2.cookie")
@Validated
public record OAuth2CookieProperties(
        @NotBlank String signingSecret
) {}
