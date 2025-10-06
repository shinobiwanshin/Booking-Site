package com.capstone.tickets.services.impl;

import com.capstone.tickets.domain.dtos.TicketBookingRequest;
import com.capstone.tickets.domain.entities.Ticket;
import com.capstone.tickets.repositories.TicketRepository;
import com.capstone.tickets.services.TicketService;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

  private final TicketRepository ticketRepository;

  @Override
  public Page<Ticket> listTicketsForUser(UUID userId, Pageable pageable) {
    return ticketRepository.findByPurchaserId(userId, pageable);
  }

  @Override
  public Optional<Ticket> getTicketForUser(UUID userId, UUID ticketId) {
    return ticketRepository.findByIdAndPurchaserId(ticketId, userId);
  }

  @Override
  public Ticket bookTickets(TicketBookingRequest request) {
    Ticket ticket = Ticket.builder()
        .status(request.getStatus())
        .ticketType(request.getTicketType())
        .purchaser(request.getPurchaser())
        .quantity(request.getQuantity()) // Set the quantity
        .build();

    return ticketRepository.save(ticket);
  }

  @Override
  public Optional<Ticket> findByQrCode(String qrCode) {
    return ticketRepository.findByQrCode(qrCode);
  }

  @Override
  public void save(Ticket ticket) {
    ticketRepository.save(ticket);
  }
}
