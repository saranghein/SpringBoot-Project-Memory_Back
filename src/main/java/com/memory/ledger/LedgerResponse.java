package com.memory.ledger;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값을 가진 필드는 JSON에 포함되지 않음
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
    private Float takedTime;


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

    private List<String> contentsList;
    public static LedgerResponse fromLedgerList(List<Ledger> ledgers) {
        List<String> contentsList = ledgers.stream()
                .map(Ledger::getContents)
                .collect(Collectors.toList());
        return LedgerResponse.builder()
                .contentsList(contentsList)
                .build();
    }
}
