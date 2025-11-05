package com.capstone.tickets.repositories;

import com.capstone.tickets.domain.entities.TicketValidation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketValidationRepository extends JpaRepository<TicketValidation, UUID> {
    @Query("SELECT COUNT(tv) FROM TicketValidation tv " +
            "JOIN tv.ticket t " +
            "JOIN t.ticketType tt " +
            "JOIN tt.event e " +
            "WHERE e.organizer.id = :organizerId AND tv.status = com.capstone.tickets.domain.entities.TicketValidationStatusEnum.VALID")
    long countValidByOrganizer(@Param("organizerId") UUID organizerId);
}
