package com.memory.ledger;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface LedgerRepository extends JpaRepository<Ledger, Long> {
    List<Ledger> findByLedgerDateAndUserId(LocalDateTime date, String userId);
    Optional<Ledger> findByRecordIdAndUserId(Long recordId, String userId);

    List<Ledger> findByUserId(String userId);
}
