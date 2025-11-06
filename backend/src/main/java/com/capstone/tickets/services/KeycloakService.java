package com.capstone.tickets.services;

import com.capstone.tickets.domain.dto.LoginRequest;
import com.capstone.tickets.domain.dto.LoginResponse;
import com.capstone.tickets.domain.dto.RegistrationRequest;
import com.capstone.tickets.domain.entities.User;
import com.capstone.tickets.domain.enums.Role;
import com.capstone.tickets.repositories.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakService {

    @Value("${keycloak.admin.url:http://localhost:9090}")
    private String keycloakAdminUrl;

    @Value("${keycloak.realm:event-ticket-platform}")
    private String realm;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    @Value("${keycloak.client.id:event-ticket-client}")
    private String clientId;

    @Value("${keycloak.client.secret:}")
    private String clientSecret;

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    /**
     * Creates a new user in Keycloak with the given registration details.
     * Returns the UUID of the created user.
     */
    public UUID createKeycloakUser(RegistrationRequest request) {
        try {
            // Get admin access token
            String accessToken = getAdminAccessToken();

            // Generate UUID for the new user
            UUID userId = UUID.randomUUID();

            // Prepare user creation request
            Map<String, Object> keycloakUser = new HashMap<>();
            keycloakUser.put("id", userId.toString());
            keycloakUser.put("username", request.username());
            keycloakUser.put("email", request.email());
            keycloakUser.put("firstName", request.name().split(" ")[0]);
            if (request.name().split(" ").length > 1) {
                keycloakUser.put("lastName", request.name().substring(request.name().indexOf(" ") + 1));
            }
            keycloakUser.put("enabled", true);
            keycloakUser.put("emailVerified", true);

            // Add credentials
            Map<String, Object> credential = new HashMap<>();
            credential.put("type", "password");
            credential.put("value", request.password());
            credential.put("temporary", false);
            keycloakUser.put("credentials", List.of(credential));

            // Add role as attribute
            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("role", List.of(request.role().name()));
            keycloakUser.put("attributes", attributes);

            // Create user in Keycloak
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(keycloakUser, headers);

            String url = String.format("%s/admin/realms/%s/users", keycloakAdminUrl, realm);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class);

            log.info("Successfully created Keycloak user: {} with ID: {}", request.username(), userId);

            // Small delay to ensure user is fully persisted in Keycloak
            Thread.sleep(1000);

            // Verify the user was created and get actual ID from Keycloak
            UUID actualUserId = getUserIdByUsername(request.username(), accessToken);
            if (actualUserId == null) {
                log.warn("Could not find user {} in Keycloak after creation. Using generated ID.", request.username());
                actualUserId = userId;
            } else if (!actualUserId.equals(userId)) {
                log.warn("Keycloak assigned different ID {} instead of requested ID {} for user {}",
                        actualUserId, userId, request.username());
                userId = actualUserId;
            }

            // Assign realm role to the user
            assignRealmRoleToUser(userId, request.role(), accessToken);

            return userId;

        } catch (Exception e) {
            log.error("Failed to create Keycloak user: {}", request.username(), e);
            throw new RuntimeException("Failed to create user in authentication system: " + e.getMessage(), e);
        }
    }

    private String getAdminAccessToken() {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("grant_type", "password");
            requestBody.put("client_id", "admin-cli");
            requestBody.put("username", adminUsername);
            requestBody.put("password", adminPassword);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            StringBuilder formData = new StringBuilder();
            requestBody.forEach((key, value) -> {
                if (formData.length() > 0) {
                    formData.append("&");
                }
                formData.append(key).append("=").append(value);
            });

            HttpEntity<String> entity = new HttpEntity<>(formData.toString(), headers);

            String url = String.format("%s/realms/master/protocol/openid-connect/token", keycloakAdminUrl);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("access_token")) {
                return (String) responseBody.get("access_token");
            }

            throw new RuntimeException("Failed to get admin access token from Keycloak");

        } catch (Exception e) {
            log.error("Failed to get Keycloak admin access token", e);
            throw new RuntimeException("Failed to authenticate with Keycloak admin", e);
        }
    }

    /**
     * Assigns a realm role to a user in Keycloak
     */
    private void assignRealmRoleToUser(UUID userId, Role role, String accessToken) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            try {
                // Get the role representation from Keycloak
                String roleName = "ROLE_" + role.name();
                Map<String, Object> roleRepresentation = getRealmRole(roleName, accessToken);

                if (roleRepresentation == null) {
                    log.warn("Role {} not found in Keycloak realm {}. Skipping role assignment.", roleName, realm);
                    return;
                }

                // Assign the role to the user
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(accessToken);

                HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(List.of(roleRepresentation), headers);

                String url = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm",
                        keycloakAdminUrl, realm, userId.toString());

                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

                log.info("Successfully assigned role {} to user {}", roleName, userId);
                return; // Success, exit the method

            } catch (Exception e) {
                lastException = e;
                retryCount++;
                if (retryCount < maxRetries) {
                    log.warn("Failed to assign role to user {} (attempt {}/{}). Retrying...", userId, retryCount,
                            maxRetries);
                    try {
                        Thread.sleep(1000); // Wait 1 second before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // All retries failed
        log.error("Failed to assign role to user {} after {} attempts", userId, maxRetries, lastException);
        // Don't throw exception - user is created, role assignment failure shouldn't
        // block registration
        log.warn("User {} created but role assignment failed. Manual role assignment may be required.", userId);
    }

    /**
     * Retrieves a realm role by name from Keycloak
     */
    private Map<String, Object> getRealmRole(String roleName, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = String.format("%s/admin/realms/%s/roles/%s",
                    keycloakAdminUrl, realm, roleName);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to get realm role: {}", roleName, e);
            return null;
        }
    }

    /**
     * Retrieves user ID by username from Keycloak
     */
    private UUID getUserIdByUsername(String username, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = String.format("%s/admin/realms/%s/users?username=%s&exact=true",
                    keycloakAdminUrl, realm, username);

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class);

            List<Map<String, Object>> users = response.getBody();
            if (users != null && !users.isEmpty()) {
                String id = (String) users.get(0).get("id");
                return UUID.fromString(id);
            }

            return null;

        } catch (Exception e) {
            log.error("Failed to get user ID for username: {}", username, e);
            return null;
        }
    }

    /**
     * Send password reset email to a user via Keycloak
     * 
     * @param email The email address to send the reset link to
     * @return Username if user found, null otherwise (for logging purposes only,
     *         not exposed to client)
     */
    public String sendPasswordResetEmail(String email) {
        try {
            // Get admin access token
            String accessToken = getAdminAccessToken();

            // Find user by email and get full details
            Map<String, Object> userDetails = getUserDetailsByEmail(email, accessToken);

            if (userDetails == null) {
                log.warn("No user found with email: {}", email);
                return null; // Don't throw exception to prevent email enumeration
            }

            String userId = (String) userDetails.get("id");
            String username = (String) userDetails.get("username");

            // Send password reset email via Keycloak using execute-actions-email
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Send array of actions with just UPDATE_PASSWORD
            String jsonBody = "[\"UPDATE_PASSWORD\"]";
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            String url = String.format("%s/admin/realms/%s/users/%s/execute-actions-email",
                    keycloakAdminUrl, realm, userId);

            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class);

            log.info("Password reset email sent successfully for user: {} with email: {}", username, email);
            return username; // Return username for logging/notification purposes

        } catch (Exception e) {
            log.error("Failed to send password reset email for: {}", email, e);
            // Don't throw exception to prevent email enumeration
            return null;
        }
    }

    /**
     * Generate password reset link for a user (returns the Keycloak reset page URL)
     * This doesn't send an email, just generates the link to be used in custom
     * emails
     * 
     * @param email The email address
     * @return Password reset link or null if user not found
     */
    public String generatePasswordResetLink(String email) {
        try {
            // Get admin access token
            String accessToken = getAdminAccessToken();

            // Find user by email
            Map<String, Object> userDetails = getUserDetailsByEmail(email, accessToken);

            if (userDetails == null) {
                log.warn("No user found with email: {}", email);
                return null;
            }

            String userId = (String) userDetails.get("id");

            // Generate the reset credentials URL
            // This URL will be sent via Keycloak's execute-actions-email endpoint
            // but we'll capture it and send it in our custom email
            String resetLink = String.format("%s/realms/%s/account/password",
                    keycloakAdminUrl.replace("/admin", ""), realm);

            log.info("Generated password reset link for email: {}", email);
            return resetLink;

        } catch (Exception e) {
            log.error("Failed to generate password reset link for: {}", email, e);
            return null;
        }
    }

    /**
     * Update user password in Keycloak
     * 
     * @param email       User's email
     * @param newPassword New password
     */
    public void updateUserPassword(String email, String newPassword) {
        try {
            // Get admin access token
            String accessToken = getAdminAccessToken();

            // Find user by email
            Map<String, Object> userDetails = getUserDetailsByEmail(email, accessToken);

            if (userDetails == null) {
                throw new RuntimeException("User not found");
            }

            String userId = (String) userDetails.get("id");

            // Update password
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create password credential
            String jsonBody = String.format(
                    "{\"type\":\"password\",\"value\":\"%s\",\"temporary\":false}",
                    newPassword);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            String url = String.format("%s/admin/realms/%s/users/%s/reset-password",
                    keycloakAdminUrl, realm, userId);

            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class);

            log.info("Password updated successfully for user with email: {}", email);

        } catch (Exception e) {
            log.error("Failed to update password for email: {}", email, e);
            throw new RuntimeException("Failed to update password: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves full user details by email from Keycloak
     */
    private Map<String, Object> getUserDetailsByEmail(String email, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = String.format("%s/admin/realms/%s/users?email=%s&exact=true",
                    keycloakAdminUrl, realm, email);

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class);

            List<Map<String, Object>> users = response.getBody();
            if (users != null && !users.isEmpty()) {
                return users.get(0);
            }

            return null;

        } catch (Exception e) {
            log.error("Failed to get user details for email: {}", email, e);
            return null;
        }
    }

    /**
     * Retrieves user ID by email from Keycloak
     */
    private UUID getUserIdByEmail(String email, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = String.format("%s/admin/realms/%s/users?email=%s&exact=true",
                    keycloakAdminUrl, realm, email);

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class);

            List<Map<String, Object>> users = response.getBody();
            if (users != null && !users.isEmpty()) {
                String id = (String) users.get(0).get("id");
                return UUID.fromString(id);
            }

            return null;

        } catch (Exception e) {
            log.error("Failed to get user ID for email: {}", email, e);
            return null;
        }
    }

    /**
     * Authenticate user with Keycloak using Direct Access Grant
     * 
     * @param request Login credentials
     * @return LoginResponse with access token and user info
     */
    public LoginResponse authenticateUser(LoginRequest request) {
        try {
            // Prepare token request
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("grant_type", "password");
            requestBody.put("client_id", clientId);
            if (clientSecret != null && !clientSecret.isEmpty()) {
                requestBody.put("client_secret", clientSecret);
            }
            requestBody.put("username", request.username());
            requestBody.put("password", request.password());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            StringBuilder formData = new StringBuilder();
            requestBody.forEach((key, value) -> {
                if (formData.length() > 0) {
                    formData.append("&");
                }
                formData.append(key).append("=").append(value);
            });

            HttpEntity<String> entity = new HttpEntity<>(formData.toString(), headers);

            String url = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakAdminUrl, realm);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("access_token")) {
                String accessToken = (String) responseBody.get("access_token");
                String refreshToken = (String) responseBody.get("refresh_token");
                Integer expiresIn = (Integer) responseBody.get("expires_in");

                // Get user info from database
                User user = getUserByUsername(request.username());

                if (user == null) {
                    throw new RuntimeException("User not found in database");
                }

                return new LoginResponse(
                        accessToken,
                        refreshToken,
                        "Bearer",
                        expiresIn != null ? expiresIn : 3600,
                        request.username(),
                        user.getEmail(),
                        user.getRole());
            }

            throw new RuntimeException("Failed to get access token from Keycloak");

        } catch (Exception e) {
            log.error("Failed to authenticate user: {}", request.username(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get user from database by username
     */
    private User getUserByUsername(String username) {
        try {
            // Get admin access token
            String accessToken = getAdminAccessToken();

            // Get user ID from Keycloak
            UUID userId = getUserIdByUsername(username, accessToken);

            if (userId == null) {
                return null;
            }

            // Fetch user from database
            return userRepository.findById(userId).orElse(null);

        } catch (Exception e) {
            log.error("Failed to get user from database: {}", username, e);
            return null;
        }
    }

    /**
     * Public method to assign a role to an existing user
     * Used by AdminService to sync roles
     */
    public void assignRoleToExistingUser(UUID userId, Role role) {
        try {
            String accessToken = getAdminAccessToken();
            assignRealmRoleToUser(userId, role, accessToken);
        } catch (Exception e) {
            log.error("Failed to assign role to user: {}", userId, e);
            throw new RuntimeException("Failed to assign role: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a realm role in Keycloak if it doesn't exist
     * 
     * @return true if role was created, false if it already existed
     */
    public boolean createRealmRoleIfNotExists(String roleName) {
        try {
            String accessToken = getAdminAccessToken();

            // Check if role exists
            Map<String, Object> existingRole = getRealmRole(roleName, accessToken);
            if (existingRole != null) {
                log.info("Role {} already exists in Keycloak", roleName);
                return false;
            }

            // Create the role
            Map<String, Object> roleData = new HashMap<>();
            roleData.put("name", roleName);
            roleData.put("description", "Application role: " + roleName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(roleData, headers);

            String url = String.format("%s/admin/realms/%s/roles", keycloakAdminUrl, realm);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            log.info("Successfully created role {} in Keycloak", roleName);
            return true;

        } catch (Exception e) {
            log.error("Failed to create role: {}", roleName, e);
            throw new RuntimeException("Failed to create role: " + e.getMessage(), e);
        }
    }
}
