package com.capstone.tickets.controllers;

import static com.capstone.tickets.util.JwtUtil.parseUserId;

import com.capstone.tickets.domain.dtos.OrganizerDashboardSummaryDto;
import com.capstone.tickets.services.DashboardService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<OrganizerDashboardSummaryDto> getSummary(
            @AuthenticationPrincipal Jwt jwt) {
        UUID organizerId = parseUserId(jwt);
        return ResponseEntity.ok(dashboardService.getOrganizerSummary(organizerId));
    }
}
