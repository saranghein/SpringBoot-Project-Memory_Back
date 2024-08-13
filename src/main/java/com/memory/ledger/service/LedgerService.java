package com.memory.ledger.service;

import com.memory.ledger.Ledger;
import com.memory.ledger.dto.LedgerResponse;
import com.memory.ledger.dto.StatisticsResponse;
import com.memory.user.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LedgerService {
    void saveLedger(Ledger ledger);

    List<LedgerResponse> getLedgerByDateAndUserId(LocalDate date, User user);

    Optional<LedgerResponse> getLedgerByRecordIdAndUserId(Long recordId, User user);

    List<Ledger> getContentsByUserIdAndDate(User user, LocalDate date);

    void deleteLedgerByRecordId(Long recordId);

    StatisticsResponse getStatistics(User user);

//    List<StatisticsResponse.EmotionDetail> getEmotionDetails(List<Ledger> ledgers, String emotionCategory);
}
