package com.capstone.tickets.services;

import com.capstone.tickets.domain.entities.Ticket;
import java.util.UUID;

public interface TicketTypeService {
  Ticket purchaseTicket(UUID userId, UUID ticketTypeId, int quantity);
}
