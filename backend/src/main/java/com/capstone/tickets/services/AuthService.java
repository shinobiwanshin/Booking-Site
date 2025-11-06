package com.capstone.tickets.services;

import com.capstone.tickets.domain.dto.LoginRequest;
import com.capstone.tickets.domain.dto.LoginResponse;
import com.capstone.tickets.domain.dto.RegistrationRequest;
import com.capstone.tickets.domain.dto.RegistrationResponse;
import com.capstone.tickets.domain.entities.User;
import com.capstone.tickets.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;

    @Transactional
    public RegistrationResponse registerUser(RegistrationRequest request) {
        log.info("Attempting to register new user: {}", request.username());

        try {
            // Create user in Keycloak first
            UUID userId = keycloakService.createKeycloakUser(request);

            // Create user in our database
            User user = new User();
            user.setId(userId);
            user.setName(request.name());
            user.setEmail(request.email());
            user.setRole(request.role());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            log.info("Successfully registered user: {} with ID: {} and role: {}",
                    request.username(), userId, request.role());

            return new RegistrationResponse(
                    userId,
                    request.username(),
                    request.email(),
                    request.name(),
                    request.role(),
                    "User registered successfully. You can now log in with your credentials.");

        } catch (Exception e) {
            log.error("Failed to register user: {}", request.username(), e);
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        }
    }

    /**
     * Send password reset email to the user
     * 
     * @param email The email address to send the reset link to
     */
    public void sendPasswordResetEmail(String email) {
        log.info("Attempting to send password reset email to: {}", email);

        try {
            // Use Keycloak's built-in password reset functionality
            keycloakService.sendPasswordResetEmail(email);
            log.info("Password reset email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            // Don't throw exception to prevent email enumeration attacks
            // The controller will return a generic success message
        }
    }

    /**
     * Authenticate user with Keycloak and return tokens
     * 
     * @param request Login credentials
     * @return LoginResponse with access token and user info
     */
    public LoginResponse loginUser(LoginRequest request) {
        log.info("Attempting to authenticate user: {}", request.username());

        try {
            // Authenticate with Keycloak and get tokens
            LoginResponse response = keycloakService.authenticateUser(request);

            log.info("Successfully authenticated user: {}", request.username());
            return response;

        } catch (Exception e) {
            log.error("Failed to authenticate user: {}", request.username(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }
}
