package com.memory.ledger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<LedgerResponse> getLedgerByDate(LocalDateTime date) {
        List<Ledger> ledgers = ledgerRepository.findByLedgerDate(date);
        return ledgers.stream()
                .map(LedgerResponse::fromLedgerWithId)
                .collect(Collectors.toList());
    }
    public List<LedgerResponse> getLedgerByDateAndUserId(LocalDateTime date, String userId) {
        List<Ledger> ledgers = ledgerRepository.findByLedgerDateAndUserId(date, userId);
        return ledgers.stream()
                .map(LedgerResponse::fromLedgerWithId)
                .collect(Collectors.toList());
    }
    public Optional<LedgerResponse> getLedgerByRecordId(Long recordId) {
        return ledgerRepository.findById(recordId)
                .map(LedgerResponse::fromLedger); //recordId 포함 X
    }

    public void deleteLedgerByRecordId(Long recordId) {
        ledgerRepository.deleteById(recordId);
    }

    public Optional<LedgerResponse> getLedgerByRecordIdAndUserId(Long recordId, String userId) {
        return ledgerRepository.findByRecordIdAndUserId(recordId, userId)
                .map(LedgerResponse::fromLedgerWithId);
    }
}
