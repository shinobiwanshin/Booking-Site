package com.capstone.tickets.controllers;

import com.capstone.tickets.services.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    /**
     * Sync all users' roles from database to Keycloak
     * This is useful when users are created but role assignment failed
     */
    @PostMapping("/sync-roles")
    public ResponseEntity<Map<String, Object>> syncAllUserRoles() {
        log.info("Received request to sync all user roles");
        try {
            Map<String, Object> result = adminService.syncAllUserRoles();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to sync user roles", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to sync roles: " + e.getMessage()));
        }
    }

    /**
     * Sync a specific user's role from database to Keycloak
     */
    @PostMapping("/sync-role/{userId}")
    public ResponseEntity<Map<String, Object>> syncUserRole(@PathVariable UUID userId) {
        log.info("Received request to sync role for user: {}", userId);
        try {
            boolean success = adminService.syncUserRole(userId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Successfully synced role for user " + userId));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "User not found or role sync failed"));
            }
        } catch (Exception e) {
            log.error("Failed to sync role for user {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to sync role: " + e.getMessage()));
        }
    }

    /**
     * Create missing realm roles in Keycloak
     */
    @PostMapping("/create-roles")
    public ResponseEntity<Map<String, Object>> createRealmRoles() {
        log.info("Received request to create realm roles");
        try {
            Map<String, Object> result = adminService.createRequiredRealmRoles();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to create realm roles", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to create roles: " + e.getMessage()));
        }
    }
}
