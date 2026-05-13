package com.tuestudio.subject.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddAliasRequest(@NotBlank @Size(max = 255) String alias) {}
