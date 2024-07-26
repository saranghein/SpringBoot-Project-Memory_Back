package com.memory.ledger;

import com.memory.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class LedgerRequest {

    //날짜
    private LocalDate ledgerDate;

    //감정
    private String emotion;

    //감정 분류
    private String emotionCategory;

    //분류
    private String category;

    //내용
    private String contents;

    //소요 시간
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
