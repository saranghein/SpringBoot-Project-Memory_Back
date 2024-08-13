package com.memory.ledger.dto;

import com.memory.ledger.Ledger;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    @Schema(description = "카테고리별 사용된 시간 목록")
    private List<TimeSpent> timeSpent;

    @Schema(description = "지난 달과의 비교")
    private ComparisonWithLastMonth comparisonWithLastMonth;

    @Schema(description = "감정 요약 목록")
    private List<EmotionSummary> emotionsSummary;

    @Schema(description = "긍정적인 감정 상세 목록")
    private List<EmotionDetail> positiveEmotions;

    @Schema(description = "중립적인 감정 상세 목록")
    private List<EmotionDetail> neutralEmotions;

    @Schema(description = "부정적인 감정 상세 목록")
    private List<EmotionDetail> negativeEmotions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSpent {
        @Schema(description = "카테고리 이름", example = "운동")
        private String category;

        @Schema(description = "소요된 시간(시간 단위)", example = "10.5")
        private float hours;

        @Schema(description = "카테고리별 시간 사용 비율", example = "15.5")
        private float percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonWithLastMonth {
        @Schema(description = "이전 달 카테고리", example = "운동")
        private String previousCategory;

        @Schema(description = "이전 달", example = "6")
        private int previousMonth;

        @Schema(description = "이전 달 소요된 시간(시간 단위)", example = "8.0")
        private float previousHours;

        @Schema(description = "현재 달 카테고리", example = "공부")
        private String currentCategory;

        @Schema(description = "현재 달", example = "7")
        private int currentMonth;

        @Schema(description = "현재 달 소요된 시간(시간 단위)", example = "12.0")
        private float currentHours;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionSummary {
        @Schema(description = "감정 유형", example = "긍정")
        private String type;

        @Schema(description = "감정 발생 횟수", example = "15")
        private int count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionDetail {
        @Schema(description = "세부 감정", example = "행복")
        private String emotions;

        @Schema(description = "감정 발생 횟수", example = "5")
        private int count;
    }

    public static StatisticsResponse of(
            List<TimeSpent> timeSpent,
            ComparisonWithLastMonth comparisonWithLastMonth,
            List<EmotionSummary> emotionsSummary,
            List<EmotionDetail> positiveEmotions,
            List<EmotionDetail> neutralEmotions,
            List<EmotionDetail> negativeEmotions
    ) {
        return StatisticsResponse.builder()
                .timeSpent(timeSpent)
                .comparisonWithLastMonth(comparisonWithLastMonth)
                .emotionsSummary(emotionsSummary)
                .positiveEmotions(positiveEmotions)
                .neutralEmotions(neutralEmotions)
                .negativeEmotions(negativeEmotions)
                .build();
    }
}
