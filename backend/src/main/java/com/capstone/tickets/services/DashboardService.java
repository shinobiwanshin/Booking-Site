package com.capstone.tickets.services;

import java.util.UUID;
import com.capstone.tickets.domain.dtos.OrganizerDashboardSummaryDto;

public interface DashboardService {
    OrganizerDashboardSummaryDto getOrganizerSummary(UUID organizerId);
}
