package com.capstone.tickets.services;

import com.capstone.tickets.domain.entities.User;
import com.capstone.tickets.domain.enums.Role;
import com.capstone.tickets.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final RestTemplate restTemplate;

    /**
     * Sync all users' roles from database to Keycloak
     */
    public Map<String, Object> syncAllUserRoles() {
        log.info("Starting to sync all user roles");
        List<User> allUsers = userRepository.findAll();

        int successCount = 0;
        int failureCount = 0;
        List<String> failedUsers = new ArrayList<>();

        for (User user : allUsers) {
            try {
                boolean success = syncUserRole(user.getId());
                if (success) {
                    successCount++;
                } else {
                    failureCount++;
                    failedUsers.add(user.getName() + " (" + user.getId() + ")");
                }
            } catch (Exception e) {
                log.error("Failed to sync role for user: {}", user.getName(), e);
                failureCount++;
                failedUsers.add(user.getName() + " (" + user.getId() + ")");
            }
        }

        log.info("Role sync completed. Success: {}, Failures: {}", successCount, failureCount);

        return Map.of(
                "success", true,
                "totalUsers", allUsers.size(),
                "successCount", successCount,
                "failureCount", failureCount,
                "failedUsers", failedUsers,
                "message", String.format("Synced roles for %d users. %d succeeded, %d failed.",
                        allUsers.size(), successCount, failureCount));
    }

    /**
     * Sync a specific user's role from database to Keycloak
     */
    public boolean syncUserRole(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found in database: {}", userId);
            return false;
        }

        User user = userOpt.get();
        log.info("Syncing role for user: {} ({}), role: {}", user.getName(), userId, user.getRole());

        try {
            // This will use the existing KeycloakService method
            // We need to expose the assignRealmRoleToUser method or create a public wrapper
            keycloakService.assignRoleToExistingUser(userId, user.getRole());
            log.info("Successfully synced role for user: {}", user.getName());
            return true;
        } catch (Exception e) {
            log.error("Failed to sync role for user: {}", user.getName(), e);
            return false;
        }
    }

    /**
     * Create all required realm roles in Keycloak if they don't exist
     */
    public Map<String, Object> createRequiredRealmRoles() {
        log.info("Creating required realm roles in Keycloak");

        List<String> createdRoles = new ArrayList<>();
        List<String> existingRoles = new ArrayList<>();
        List<String> failedRoles = new ArrayList<>();

        // Roles to create
        String[] roles = { "ROLE_ATTENDEE", "ROLE_ORGANIZER", "ROLE_STAFF" };

        for (String roleName : roles) {
            try {
                boolean created = keycloakService.createRealmRoleIfNotExists(roleName);
                if (created) {
                    createdRoles.add(roleName);
                } else {
                    existingRoles.add(roleName);
                }
            } catch (Exception e) {
                log.error("Failed to create role: {}", roleName, e);
                failedRoles.add(roleName);
            }
        }

        log.info("Role creation completed. Created: {}, Existing: {}, Failed: {}",
                createdRoles.size(), existingRoles.size(), failedRoles.size());

        return Map.of(
                "success", failedRoles.isEmpty(),
                "createdRoles", createdRoles,
                "existingRoles", existingRoles,
                "failedRoles", failedRoles,
                "message", String.format("Created %d roles, %d already existed, %d failed.",
                        createdRoles.size(), existingRoles.size(), failedRoles.size()));
    }
}
