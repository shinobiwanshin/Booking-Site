package com.capstone.tickets.services.impl;

import com.capstone.tickets.domain.dtos.OrganizerDashboardSummaryDto;
import com.capstone.tickets.domain.entities.EventStatusEnum;
import com.capstone.tickets.repositories.EventRepository;
import com.capstone.tickets.repositories.TicketRepository;
import com.capstone.tickets.repositories.TicketTypeRepository;
import com.capstone.tickets.repositories.TicketValidationRepository;
import com.capstone.tickets.services.DashboardService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final TicketValidationRepository ticketValidationRepository;

    @Override
    public OrganizerDashboardSummaryDto getOrganizerSummary(UUID organizerId) {
        long totalEvents = eventRepository.countByOrganizerId(organizerId);
        long publishedEvents = eventRepository.countByOrganizerIdAndStatus(organizerId, EventStatusEnum.PUBLISHED);
        long draftEvents = eventRepository.countByOrganizerIdAndStatus(organizerId, EventStatusEnum.DRAFT);
        long totalTicketTypes = ticketTypeRepository.countByOrganizer(organizerId);
        Long availableSum = ticketTypeRepository.sumTotalAvailableByOrganizer(organizerId);
        long totalTicketsAvailable = availableSum == null ? 0L : availableSum;
        long totalTicketsSold = ticketRepository.countByOrganizer(organizerId);
        Double revenue = ticketRepository.sumRevenueByOrganizer(organizerId);
        long totalAttendances = ticketValidationRepository.countValidByOrganizer(organizerId);

        return OrganizerDashboardSummaryDto.builder()
                .totalEvents(totalEvents)
                .publishedEvents(publishedEvents)
                .draftEvents(draftEvents)
                .totalTicketTypes(totalTicketTypes)
                .totalTicketsAvailable(totalTicketsAvailable)
                .totalTicketsSold(totalTicketsSold)
                .totalRevenue(revenue == null ? 0.0 : revenue)
                .totalAttendances(totalAttendances)
                .build();
    }
}
