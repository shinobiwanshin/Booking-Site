package com.capstone.tickets.services;

import com.capstone.tickets.domain.entities.Ticket;

/**
 * Simple service to generate a PDF representation of a ticket.
 */
public interface PdfService {
    byte[] generateTicketPdf(Ticket ticket, byte[] qrPngBytes);
}
