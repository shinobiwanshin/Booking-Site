package com.capstone.tickets.mappers;

import com.capstone.tickets.domain.CreateEventRequest;
import com.capstone.tickets.domain.CreateTicketTypeRequest;
import com.capstone.tickets.domain.UpdateEventRequest;
import com.capstone.tickets.domain.UpdateTicketTypeRequest;
import com.capstone.tickets.domain.dtos.CreateEventRequestDto;
import com.capstone.tickets.domain.dtos.CreateEventResponseDto;
import com.capstone.tickets.domain.dtos.CreateTicketTypeRequestDto;
import com.capstone.tickets.domain.dtos.GetEventDetailsResponseDto;
import com.capstone.tickets.domain.dtos.GetEventDetailsTicketTypesResponseDto;
import com.capstone.tickets.domain.dtos.GetPublishedEventDetailsResponseDto;
import com.capstone.tickets.domain.dtos.GetPublishedEventDetailsTicketTypesResponseDto;
import com.capstone.tickets.domain.dtos.ListEventResponseDto;
import com.capstone.tickets.domain.dtos.ListEventTicketTypeResponseDto;
import com.capstone.tickets.domain.dtos.ListPublishedEventResponseDto;
import com.capstone.tickets.domain.dtos.UpdateEventRequestDto;
import com.capstone.tickets.domain.dtos.UpdateEventResponseDto;
import com.capstone.tickets.domain.dtos.UpdateTicketTypeRequestDto;
import com.capstone.tickets.domain.dtos.UpdateTicketTypeResponseDto;
import com.capstone.tickets.domain.entities.Event;
import com.capstone.tickets.domain.entities.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

  CreateTicketTypeRequest fromDto(CreateTicketTypeRequestDto dto);

  CreateEventRequest fromDto(CreateEventRequestDto dto);

  CreateEventResponseDto toDto(Event event);

  ListEventTicketTypeResponseDto toDto(TicketType ticketType);

  ListEventResponseDto toListEventResponseDto(Event event);

  GetEventDetailsTicketTypesResponseDto toGetEventDetailsTicketTypesResponseDto(
      TicketType ticketType);

  GetEventDetailsResponseDto toGetEventDetailsResponseDto(Event event);

  UpdateTicketTypeRequest fromDto(UpdateTicketTypeRequestDto dto);

  UpdateEventRequest fromDto(UpdateEventRequestDto dto);

  UpdateTicketTypeResponseDto toUpdateTicketTypeResponseDto(TicketType ticketType);

  UpdateEventResponseDto toUpdateEventResponseDto(Event event);

  ListPublishedEventResponseDto toListPublishedEventResponseDto(Event event);

  GetPublishedEventDetailsTicketTypesResponseDto toGetPublishedEventDetailsTicketTypesResponseDto(
      TicketType ticketType);

  GetPublishedEventDetailsResponseDto toGetPublishedEventDetailsResponseDto(Event event);
}
