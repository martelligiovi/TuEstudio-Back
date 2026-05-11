package com.tuestudio.auth.infrastructure.security;

public sealed interface OAuthCallbackResult {
    record Success(String token) implements OAuthCallbackResult {}
    record Failure(String reason) implements OAuthCallbackResult {}
}
