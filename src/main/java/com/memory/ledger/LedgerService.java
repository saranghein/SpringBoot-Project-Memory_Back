package com.memory.ledger;

import com.memory.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
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


    public List<LedgerResponse> getLedgerByDateAndUserId(LocalDate date, User user) {
        List<Ledger> ledgers = ledgerRepository.findByLedgerDateAndUser(date, user);
        return ledgers.stream()
                .map(LedgerResponse::fromLedgerWithId)
                .collect(Collectors.toList());
    }

    public Optional<LedgerResponse> getLedgerByRecordIdAndUserId(Long recordId, User user) {
        return ledgerRepository.findByRecordIdAndUser(recordId, user)
                .map(LedgerResponse::fromLedger);
    }

    public List<Ledger> getContentsByUserIdAndDate(User user, LocalDate date) {
        return ledgerRepository.findByLedgerDateAndUser(date, user);
    }

    public void deleteLedgerByRecordId(Long recordId) {
        ledgerRepository.deleteById(recordId);
    }

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

        // ComparisonWithLastMonth
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

        // Compute emotions summary
        Map<String, Long> emotionSummaryMap = currentMonthLedgers.stream()
                .collect(Collectors.groupingBy(Ledger::getEmotionCategory, Collectors.counting()));

        List<StatisticsResponse.EmotionSummary> emotionsSummary = emotionSummaryMap.entrySet().stream()
                .map(entry -> StatisticsResponse.EmotionSummary.builder()
                        .type(entry.getKey())
                        .count(entry.getValue().intValue())
                        .build())
                .collect(Collectors.toList());

        // Compute emotion details
        List<StatisticsResponse.EmotionDetail> positiveEmotions = getEmotionDetails(currentMonthLedgers, "긍정");
        List<StatisticsResponse.EmotionDetail> neutralEmotions = getEmotionDetails(currentMonthLedgers, "중립");
        List<StatisticsResponse.EmotionDetail> negativeEmotions = getEmotionDetails(currentMonthLedgers, "부정");

        return StatisticsResponse.builder()
                .timeSpent(timeSpent)
                .comparisonWithLastMonth(comparisonWithLastMonth)
                .emotionsSummary(emotionsSummary)
                .positiveEmotions(positiveEmotions)
                .neutralEmotions(neutralEmotions)
                .negativeEmotions(negativeEmotions)
                .build();
    }

    private List<StatisticsResponse.EmotionDetail> getEmotionDetails(List<Ledger> ledgers, String emotionCategory) {
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