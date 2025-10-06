package com.capstone.tickets.domain.dtos;

import com.capstone.tickets.domain.entities.TicketStatusEnum;
import com.capstone.tickets.domain.entities.TicketType;
import com.capstone.tickets.domain.entities.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketBookingRequest {
    private TicketStatusEnum status;
    private TicketType ticketType;
    private User purchaser;
    private int quantity; // Number of tickets to book
}