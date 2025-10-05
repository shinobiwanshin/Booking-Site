package com.capstone.tickets.services;

import com.capstone.tickets.domain.entities.QrCode;
import com.capstone.tickets.domain.entities.Ticket;
import java.util.UUID;

public interface QrCodeService {

  QrCode generateQrCode(Ticket ticket);

  byte[] getQrCodeImageForUserAndTicket(UUID userId, UUID ticketId);
}
