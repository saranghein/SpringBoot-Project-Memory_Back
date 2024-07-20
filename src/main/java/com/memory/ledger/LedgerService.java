package com.memory.ledger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LedgerService {
    LedgerRepository ledgerRepository;
    @Autowired
    public LedgerService(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    public void saveLedger(Ledger ledger) {
        ledgerRepository.save(ledger);
    }


    public List<LedgerResponse> getLedgerByDateAndUserId(LocalDateTime date, String userId) {
        List<Ledger> ledgers = ledgerRepository.findByLedgerDateAndUserId(date, userId);
        return ledgers.stream()
                .map(LedgerResponse::fromLedgerWithId)
                .collect(Collectors.toList());
    }

    public Optional<LedgerResponse> getLedgerByRecordIdAndUserId(Long recordId, String userId) {
        return ledgerRepository.findByRecordIdAndUserId(recordId, userId)
                .map(LedgerResponse::fromLedger);
    }

    public List<Ledger> getContentsByUserIdAndDate(String userId, LocalDateTime date) {
        return ledgerRepository.findByLedgerDateAndUserId(date, userId);
    }
    public void deleteLedgerByRecordId(Long recordId) {
        ledgerRepository.deleteById(recordId);
    }

}
