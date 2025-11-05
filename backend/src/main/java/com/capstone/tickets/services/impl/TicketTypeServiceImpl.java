package com.capstone.tickets.services.impl;

import com.capstone.tickets.domain.entities.Ticket;
import com.capstone.tickets.domain.entities.TicketStatusEnum;
import com.capstone.tickets.domain.entities.TicketType;
import com.capstone.tickets.domain.entities.User;
import com.capstone.tickets.exceptions.TicketTypeNotFoundException;
import com.capstone.tickets.exceptions.TicketsSoldOutException;
import com.capstone.tickets.exceptions.UserNotFoundException;
import com.capstone.tickets.repositories.TicketRepository;
import com.capstone.tickets.repositories.TicketTypeRepository;
import com.capstone.tickets.repositories.UserRepository;
import com.capstone.tickets.services.QrCodeService;
import com.capstone.tickets.services.TicketTypeService;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

  private final UserRepository userRepository;
  private final TicketTypeRepository ticketTypeRepository;
  private final TicketRepository ticketRepository;
  private final QrCodeService qrCodeService;

  @Override
  @Transactional
  public Ticket purchaseTicket(UUID userId, UUID ticketTypeId, int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than 0");
    }

    User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
        String.format("User with ID %s was not found", userId)));

    TicketType ticketType = ticketTypeRepository.findByIdWithLock(ticketTypeId)
        .orElseThrow(() -> new TicketTypeNotFoundException(
            String.format("Ticket type with ID %s was not found", ticketTypeId)));

    int purchasedTickets = ticketRepository.countByTicketTypeId(ticketType.getId());
    Integer totalAvailable = ticketType.getTotalAvailable();

    if (purchasedTickets + quantity > totalAvailable) {
      throw new TicketsSoldOutException();
    }

    Ticket ticket = new Ticket();
    ticket.setStatus(TicketStatusEnum.PURCHASED);
    ticket.setTicketType(ticketType);
    ticket.setPurchaser(user);
    ticket.setQuantity(quantity);

    Ticket savedTicket = ticketRepository.save(ticket);
    qrCodeService.generateQrCode(savedTicket);

    return ticketRepository.save(savedTicket);
  }
}
