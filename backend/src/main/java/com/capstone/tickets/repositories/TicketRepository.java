package com.capstone.tickets.repositories;

import com.capstone.tickets.domain.entities.Ticket;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
  // Place this method inside the TicketRepository interface
  // Optional<Ticket> findByQrCode(String qrCode);
  @Query("SELECT t FROM Ticket t JOIN t.qrCodes q WHERE q.value = :qrCode")
  Optional<Ticket> findByQrCode(@Param("qrCode") String qrCode);

  int countByTicketTypeId(UUID ticketTypeId);

  Page<Ticket> findByPurchaserId(UUID purchaserId, Pageable pageable);

  Optional<Ticket> findByIdAndPurchaserId(UUID id, UUID purchaserId);
}
