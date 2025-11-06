package com.capstone.tickets.controllers;

import com.capstone.tickets.domain.dto.ForgotPasswordRequest;
import com.capstone.tickets.domain.dto.LoginRequest;
import com.capstone.tickets.domain.dto.LoginResponse;
import com.capstone.tickets.domain.dto.RegistrationRequest;
import com.capstone.tickets.domain.dto.RegistrationResponse;
import com.capstone.tickets.domain.dto.ResetPasswordRequest;
import com.capstone.tickets.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request) {

        log.info("Received registration request for username: {}", request.username());

        try {
            RegistrationResponse response = authService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Registration failed for username: {}", request.username(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegistrationResponse(
                            null,
                            request.username(),
                            request.email(),
                            request.name(),
                            request.role(),
                            "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        log.info("Received forgot password request for email: {}", request.email());

        try {
            authService.sendPasswordResetEmail(request.email());
            return ResponseEntity.ok(Map.of(
                    "message", "If an account exists with that email, you will receive password reset instructions."));
        } catch (Exception e) {
            log.error("Forgot password failed for email: {}", request.email(), e);
            // Return success message even on error to prevent email enumeration
            return ResponseEntity.ok(Map.of(
                    "message", "If an account exists with that email, you will receive password reset instructions."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        log.info("Received reset password request");

        try {
            authService.resetPassword(request.token(), request.newPassword());
            return ResponseEntity.ok(Map.of(
                    "message", "Your password has been reset successfully. You can now login with your new password."));
        } catch (Exception e) {
            log.error("Password reset failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for username: {}", request.username());

        try {
            LoginResponse response = authService.loginUser(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for username: {}", request.username(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
    }
}
