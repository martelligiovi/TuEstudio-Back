package com.tuestudio.auth.application.usecase;

public record LoginCommand(String email, String rawPassword) {}
