package com.tuestudio.auth.infrastructure.web;

import com.tuestudio.auth.application.usecase.LoginUseCase;
import com.tuestudio.auth.application.usecase.RegisterUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public final class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterUseCase registerUseCase, LoginUseCase loginUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.from(registerUseCase.register(request.toCommand())));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(AuthResponse.from(loginUseCase.login(request.toCommand())));
    }
}
