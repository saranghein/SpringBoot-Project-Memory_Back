package com.memory.ledger.service;

import com.memory.ledger.Ledger;
import com.memory.ledger.LedgerRepository;
import com.memory.ledger.dto.LedgerResponse;
import com.memory.ledger.dto.StatisticsResponse;
import com.memory.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class LedgerServiceImpl implements LedgerService {
    LedgerRepository ledgerRepository;

    @Autowired
    public LedgerServiceImpl(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @Override
    public void saveLedger(Ledger ledger) {
        ledgerRepository.save(ledger);
    }


    @Override
    public List<LedgerResponse> getLedgerByDateAndUserId(LocalDate date, User user) {
        List<Ledger> ledgers = ledgerRepository.findByLedgerDateAndUser(date, user);
        return ledgers.stream()
                .map(LedgerResponse::fromLedgerWithId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LedgerResponse> getLedgerByRecordIdAndUserId(Long recordId, User user) {
        return ledgerRepository.findByRecordIdAndUser(recordId, user)
                .map(LedgerResponse::fromLedger);
    }

    @Override
    public List<Ledger> getContentsByUserIdAndDate(User user, LocalDate date) {
        return ledgerRepository.findByLedgerDateAndUser(date, user);
    }

    @Override
    public void deleteLedgerByRecordId(Long recordId) {
        ledgerRepository.deleteById(recordId);
    }

    @Override
    public StatisticsResponse getStatistics(User user) {
        List<Ledger> ledgers = ledgerRepository.findByUser(user);

        LocalDate now = LocalDate.now();
        LocalDate startOfCurrentMonth = now.withDayOfMonth(1);
        LocalDate startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);

        // 이번 달 데이터 필터링
        List<Ledger> currentMonthLedgers = ledgers.stream()
                .filter(ledger -> ledger.getLedgerDate().isAfter(startOfCurrentMonth))
                .collect(Collectors.toList());

        // TimeSpent
        List<StatisticsResponse.TimeSpent> timeSpent = getTimeSpent(currentMonthLedgers);

        // ComparisonWithLastMonth
        StatisticsResponse.ComparisonWithLastMonth comparisonWithLastMonth = getComparisonWithLastMonth(ledgers, startOfPreviousMonth, startOfCurrentMonth, currentMonthLedgers);

        // Compute emotions summary
        List<StatisticsResponse.EmotionSummary> emotionsSummary = getEmotionSummaries(currentMonthLedgers);

        // Compute emotion details
        List<StatisticsResponse.EmotionDetail> positiveEmotions = getEmotionDetails(currentMonthLedgers, "긍정");
        List<StatisticsResponse.EmotionDetail> neutralEmotions = getEmotionDetails(currentMonthLedgers, "중립");
        List<StatisticsResponse.EmotionDetail> negativeEmotions = getEmotionDetails(currentMonthLedgers, "부정");

        return StatisticsResponse.of(
                timeSpent,
                comparisonWithLastMonth,
                emotionsSummary,
                positiveEmotions,
                neutralEmotions,
                negativeEmotions
        );
    }

    private static List<StatisticsResponse.EmotionSummary> getEmotionSummaries(List<Ledger> currentMonthLedgers) {
        Map<String, Long> emotionSummaryMap = currentMonthLedgers.stream()
                .collect(Collectors.groupingBy(Ledger::getEmotionCategory, Collectors.counting()));

        List<StatisticsResponse.EmotionSummary> emotionsSummary = emotionSummaryMap.entrySet().stream()
                .map(entry -> StatisticsResponse.EmotionSummary.builder()
                        .type(entry.getKey())
                        .count(entry.getValue().intValue())
                        .build())
                .collect(Collectors.toList());
        return emotionsSummary;
    }

    private static StatisticsResponse.ComparisonWithLastMonth getComparisonWithLastMonth(List<Ledger> ledgers, LocalDate startOfPreviousMonth, LocalDate startOfCurrentMonth, List<Ledger> currentMonthLedgers) {
        List<Ledger> previousMonthLedgers = ledgers.stream()
                .filter(ledger -> ledger.getLedgerDate().isAfter(startOfPreviousMonth) && ledger.getLedgerDate().isBefore(startOfCurrentMonth))
                .toList();

        Map<String, Double> previousMonthData = previousMonthLedgers.stream()
                .collect(Collectors.groupingBy(Ledger::getCategory, Collectors.summingDouble(Ledger::getTakedTime)));

        Map<String, Double> currentMonthData = currentMonthLedgers.stream()
                .collect(Collectors.groupingBy(Ledger::getCategory, Collectors.summingDouble(Ledger::getTakedTime)));

        StatisticsResponse.ComparisonWithLastMonth comparisonWithLastMonth = StatisticsResponse.ComparisonWithLastMonth.builder()
                .previousCategory(previousMonthData.entrySet().stream().max(Map.Entry.comparingByValue(Double::compareTo)).map(Map.Entry::getKey).orElse(""))
                .previousMonth(startOfPreviousMonth.getMonthValue())
                .previousHours(previousMonthData.values().stream().max(Double::compareTo).orElse(0.0).floatValue())
                .currentCategory(currentMonthData.entrySet().stream().max(Map.Entry.comparingByValue(Double::compareTo)).map(Map.Entry::getKey).orElse(""))
                .currentMonth(startOfCurrentMonth.getMonthValue())
                .currentHours(currentMonthData.values().stream().max(Double::compareTo).orElse(0.0).floatValue())
                .build();
        return comparisonWithLastMonth;
    }

    private static List<StatisticsResponse.TimeSpent> getTimeSpent(List<Ledger> currentMonthLedgers) {
        Map<String, Double> categoryTimeMap = new HashMap<>();
        currentMonthLedgers.stream()
                .filter(ledger -> ledger.getTakedTime() > 0)
                .forEach(ledger -> categoryTimeMap.merge(ledger.getCategory(), (double) ledger.getTakedTime(), Double::sum));

        double totalTime = categoryTimeMap.values().stream().mapToDouble(Double::doubleValue).sum();
        List<StatisticsResponse.TimeSpent> timeSpent = categoryTimeMap.entrySet().stream()
                .map(entry -> StatisticsResponse.TimeSpent.builder()
                        .category(entry.getKey())
                        .hours(entry.getValue().floatValue())
                        .percentage((float) ((entry.getValue() / totalTime) * 100))
                        .build())
                .sorted(Comparator.comparingDouble(StatisticsResponse.TimeSpent::getHours).reversed())
                .limit(5)
                .collect(Collectors.toList());
        return timeSpent;
    }

    private static List<StatisticsResponse.EmotionDetail> getEmotionDetails(List<Ledger> ledgers, String emotionCategory) {
        return ledgers.stream()
                .filter(ledger -> emotionCategory.equals(ledger.getEmotionCategory()))
                .collect(Collectors.groupingBy(Ledger::getEmotion, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> StatisticsResponse.EmotionDetail.builder()
                        .emotions(entry.getKey())
                        .count(entry.getValue().intValue())
                        .build())
                .collect(Collectors.toList());
    }
}