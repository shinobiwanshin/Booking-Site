package com.devtiro.tickets.domain.dtos;

import com.devtiro.tickets.domain.entities.TicketStatusEnum;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListTicketResponseDto {
  private UUID id;
  private TicketStatusEnum status;
  private ListTicketTicketTypeResponseDto ticketType;
}
