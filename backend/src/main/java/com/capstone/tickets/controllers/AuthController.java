package com.capstone.tickets.controllers;

import com.capstone.tickets.domain.dto.RegistrationRequest;
import com.capstone.tickets.domain.dto.RegistrationResponse;
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
}
