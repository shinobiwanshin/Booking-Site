package com.devtiro.tickets.repositories;

import com.devtiro.tickets.domain.entities.QrCode;
import com.devtiro.tickets.domain.entities.QrCodeStatusEnum;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {
  Optional<QrCode> findByTicketIdAndTicketPurchaserId(UUID ticketId, UUID ticketPurchaseId);
  Optional<QrCode> findByIdAndStatus(UUID id, QrCodeStatusEnum status);
}
