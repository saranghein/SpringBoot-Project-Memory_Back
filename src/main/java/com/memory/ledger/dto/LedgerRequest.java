package com.memory.ledger.dto;

import com.memory.ledger.Ledger;
import com.memory.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class LedgerRequest {

    //날짜
    @Schema(description = "가계부 작성 날짜", example = "2024-07-30")
    private LocalDate ledgerDate;

    //감정
    @Schema(description = "감정", example = "행복")
    private String emotion;

    //감정 분류
    @Schema(description = "감정 분류", example = "긍정")
    private String emotionCategory;

    //분류
    @Schema(description = "분류", example = "만남")
    private String category;

    //내용
    @Schema(description = "내용", example = "조깅")
    private String contents;

    //소요 시간
    @Schema(description = "소요 시간", example = "1")
    private float takedTime;

    public Ledger toLedger(User user) {
        return Ledger.builder()
                .ledgerDate(this.ledgerDate)
                .emotion(this.emotion)
                .emotionCategory(this.emotionCategory)
                .category(this.category)
                .contents(this.contents)
                .takedTime(this.takedTime)
                .user(user)//userId
                .build();
    }

}
