package com.memory.ledger;

import com.memory.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface LedgerRepository extends JpaRepository<Ledger, Long> {
    List<Ledger> findByLedgerDateAndUser(LocalDate date, User user);
    Optional<Ledger> findByRecordIdAndUser(Long recordId, User user);
    List<Ledger> findByUser(User user);
}
