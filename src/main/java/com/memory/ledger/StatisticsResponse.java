package com.memory.ledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private List<TimeSpent> timeSpent;
    private ComparisonWithLastMonth comparisonWithLastMonth;
    private List<EmotionSummary> emotionsSummary;
    private List<EmotionDetail> positiveEmotions;
    private List<EmotionDetail> neutralEmotions;
    private List<EmotionDetail> negativeEmotions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSpent {
        private String category;
        private float hours;
        private float percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonWithLastMonth {
        private String previousCategory;
        private int previousMonth;
        private float previousHours;
        private String currentCategory;
        private int currentMonth;
        private float currentHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionSummary {
        private String type;
        private int count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionDetail {
        private String emotions;
        private int count;
    }
}
