package com.capstone.tickets.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizerDashboardSummaryDto {
    private long totalEvents;
    private long publishedEvents;
    private long draftEvents;
    private long totalTicketTypes;
    private long totalTicketsAvailable;
    private long totalTicketsSold;
    private double totalRevenue;
    private long totalAttendances;
}
