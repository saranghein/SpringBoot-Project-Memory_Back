package com.memory.ledger;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LedgerResponse {

    //recordId
    private Long recordId;

    //감정
    private String emotion;

    //분류
    private String category;

    //내용
    private String contents;

    //소요 시간
    private float takedTime;

    public static LedgerResponse fromLedgerWithId(Ledger ledger) {
        return LedgerResponse.builder()
                .recordId(ledger.getRecordId())
                .emotion(ledger.getEmotion())
                .category(ledger.getCategory())
                .contents(ledger.getContents())
                .takedTime(ledger.getTakedTime())
                .build();
    }

    public static LedgerResponse fromLedger(Ledger ledger) {
        return LedgerResponse.builder()
                .emotion(ledger.getEmotion())
                .category(ledger.getCategory())
                .contents(ledger.getContents())
                .takedTime(ledger.getTakedTime())
                .build();
    }
}
