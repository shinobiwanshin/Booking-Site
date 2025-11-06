package com.capstone.tickets.services;

import com.capstone.tickets.domain.dto.LoginRequest;
import com.capstone.tickets.domain.dto.LoginResponse;
import com.capstone.tickets.domain.dto.RegistrationRequest;
import com.capstone.tickets.domain.dto.RegistrationResponse;
import com.capstone.tickets.domain.entities.PasswordResetToken;
import com.capstone.tickets.domain.entities.User;
import com.capstone.tickets.repositories.PasswordResetTokenRepository;
import com.capstone.tickets.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

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
    @Transactional
    public void sendPasswordResetEmail(String email) {
        log.info("Attempting to send password reset email to: {}", email);

        try {
            // Find user in database
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                log.warn("No user found with email: {}", email);
                return; // Don't throw exception to prevent email enumeration
            }

            // Delete any existing tokens for this email
            passwordResetTokenRepository.deleteByEmail(email);

            // Generate new token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .email(email)
                    .expiryDate(LocalDateTime.now().plusHours(24))
                    .used(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            passwordResetTokenRepository.save(resetToken);

            // Create reset link pointing to frontend
            String resetLink = String.format("%s/reset-password?token=%s", frontendUrl, token);

            // Send custom HTML email
            emailService.sendPasswordResetEmail(email, resetLink, user.getName());
            log.info("Password reset email sent successfully to: {} (username: {})", email, user.getName());

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            // Don't throw exception to prevent email enumeration attacks
        }
    }

    /**
     * Reset user password using token
     * 
     * @param token       The reset token
     * @param newPassword The new password
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Attempting to reset password with token");

        // Find token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Check if token is expired
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        // Check if token already used
        if (resetToken.isUsed()) {
            throw new RuntimeException("Reset token has already been used");
        }

        // Update password in Keycloak
        keycloakService.updateUserPassword(resetToken.getEmail(), newPassword);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successfully for email: {}", resetToken.getEmail());
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
