package com.devtiro.tickets.mappers;

import com.devtiro.tickets.domain.CreateEventRequest;
import com.devtiro.tickets.domain.CreateTicketTypeRequest;
import com.devtiro.tickets.domain.UpdateEventRequest;
import com.devtiro.tickets.domain.UpdateTicketTypeRequest;
import com.devtiro.tickets.domain.dtos.CreateEventRequestDto;
import com.devtiro.tickets.domain.dtos.CreateEventResponseDto;
import com.devtiro.tickets.domain.dtos.CreateTicketTypeRequestDto;
import com.devtiro.tickets.domain.dtos.GetEventDetailsResponseDto;
import com.devtiro.tickets.domain.dtos.GetEventDetailsTicketTypesResponseDto;
import com.devtiro.tickets.domain.dtos.GetPublishedEventDetailsResponseDto;
import com.devtiro.tickets.domain.dtos.GetPublishedEventDetailsTicketTypesResponseDto;
import com.devtiro.tickets.domain.dtos.ListEventResponseDto;
import com.devtiro.tickets.domain.dtos.ListEventTicketTypeResponseDto;
import com.devtiro.tickets.domain.dtos.ListPublishedEventResponseDto;
import com.devtiro.tickets.domain.dtos.UpdateEventRequestDto;
import com.devtiro.tickets.domain.dtos.UpdateEventResponseDto;
import com.devtiro.tickets.domain.dtos.UpdateTicketTypeRequestDto;
import com.devtiro.tickets.domain.dtos.UpdateTicketTypeResponseDto;
import com.devtiro.tickets.domain.entities.Event;
import com.devtiro.tickets.domain.entities.TicketType;
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
