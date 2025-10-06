package com.capstone.tickets.controllers;

import static com.capstone.tickets.util.JwtUtil.parseUserId;

import com.capstone.tickets.domain.dtos.GetTicketResponseDto;
import com.capstone.tickets.domain.dtos.ListTicketResponseDto;
import com.capstone.tickets.domain.dtos.TicketBookingRequest;
import com.capstone.tickets.domain.entities.Ticket;
import com.capstone.tickets.mappers.TicketMapper;
import com.capstone.tickets.services.QrCodeService;
import com.capstone.tickets.services.TicketService;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

  private final TicketService ticketService;
  private final TicketMapper ticketMapper;
  private final QrCodeService qrCodeService;

  @PostMapping("/validate/{qrCode}")
  public ResponseEntity<String> validateTicket(@PathVariable String qrCode) {
    Optional<Ticket> ticketOptional = ticketService.findByQrCode(qrCode);

    if (ticketOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket not found");
    }

    Ticket ticket = ticketOptional.get();

    if (ticket.getQuantity() > 0) {
      ticket.setQuantity(ticket.getQuantity() - 1); // Decrement the quantity
      ticketService.save(ticket); // Save the updated ticket
      return ResponseEntity.ok("Ticket validated. Remaining scans: " + ticket.getQuantity());
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No remaining scans for this ticket");
    }
  }

  // Place this inside the TicketController class
  @PostMapping("/book")
  public ResponseEntity<Ticket> bookTickets(@RequestBody TicketBookingRequest request) {
    Ticket ticket = ticketService.bookTickets(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
  }

  @GetMapping
  public Page<ListTicketResponseDto> listTickets(
      @AuthenticationPrincipal Jwt jwt,
      Pageable pageable
  ) {
    return ticketService.listTicketsForUser(
        parseUserId(jwt),
        pageable
    ).map(ticketMapper::toListTicketResponseDto);
  }

  @GetMapping(path = "/{ticketId}")
  public ResponseEntity<GetTicketResponseDto> getTicket(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID ticketId
  ) {
    return ticketService
        .getTicketForUser(parseUserId(jwt), ticketId)
        .map(ticketMapper::toGetTicketResponseDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(path = "/{ticketId}/qr-codes")
  public ResponseEntity<byte[]> getTicketQrCode(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID ticketId
  ) {
    byte[] qrCodeImage = qrCodeService.getQrCodeImageForUserAndTicket(
        parseUserId(jwt),
        ticketId
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    headers.setContentLength(qrCodeImage.length);

    return ResponseEntity.ok()
        .headers(headers)
        .body(qrCodeImage);
  }

}
