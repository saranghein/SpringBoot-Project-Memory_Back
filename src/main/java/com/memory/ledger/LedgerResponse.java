package com.memory.ledger;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값을 가진 필드는 JSON에 포함되지 않음
public class LedgerResponse {

    //recordId
    @Schema(description = "레코드 ID", example = "1")
    private Long recordId;

    //감정
    @Schema(description = "감정", example = "행복")
    private String emotion;

    //분류
    @Schema(description = "분류", example = "만남")
    private String category;

    //내용
    @Schema(description = "내용", example = "조깅")
    private String contents;

    //소요 시간
    @Schema(description = "소요 시간", example = "1")
    private Float takedTime;

    private List<String> contentsList;


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
    public static LedgerResponse fromLedgerList(List<Ledger> ledgers) {
        List<String> contentsList = ledgers.stream()
                .map(Ledger::getContents)
                .collect(Collectors.toList());
        return LedgerResponse.builder()
                .contentsList(contentsList)
                .build();
    }
}
