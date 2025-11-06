package com.capstone.tickets.domain.dto;

import com.capstone.tickets.domain.enums.Role;

public record LoginResponse(
        String access_token,
        String refresh_token,
        String token_type,
        int expires_in,
        String username,
        String email,
        Role role) {
}
