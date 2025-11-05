package com.capstone.tickets.controllers;

import static com.capstone.tickets.util.JwtUtil.parseUserId;

import com.capstone.tickets.services.TicketTypeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/events/{eventId}/ticket-types")
public class TicketTypeController {

  private final TicketTypeService ticketTypeService;

  @PostMapping(path = "/{ticketTypeId}/tickets")
  public ResponseEntity<Void> purchaseTicket(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID ticketTypeId,
      @RequestBody(required = false) PurchaseRequest request) {
    int quantity = (request != null && request.quantity() != null) ? request.quantity() : 1;
    ticketTypeService.purchaseTicket(parseUserId(jwt), ticketTypeId, quantity);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  public record PurchaseRequest(Integer quantity) {
  }
}
