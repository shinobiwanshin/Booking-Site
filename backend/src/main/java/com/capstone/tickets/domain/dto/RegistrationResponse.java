package com.capstone.tickets.domain.dto;

import com.capstone.tickets.domain.enums.Role;
import java.util.UUID;

public record RegistrationResponse(
        UUID userId,
        String username,
        String email,
        String name,
        Role role,
        String message) {
}
