package com.capstone.tickets.services;

import com.capstone.tickets.domain.dtos.TicketBookingRequest;
import com.capstone.tickets.domain.entities.Ticket;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {
  Ticket bookTickets(TicketBookingRequest request);

  Optional<Ticket> findByQrCode(String qrCode);

  void save(Ticket ticket);

  Page<Ticket> listTicketsForUser(UUID userId, Pageable pageable);

  Optional<Ticket> getTicketForUser(UUID userId, UUID ticketId);
}
