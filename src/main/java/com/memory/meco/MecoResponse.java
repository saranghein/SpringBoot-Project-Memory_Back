package com.memory.meco;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값을 가진 필드는 JSON에 포함되지 않음
public class MecoResponse {
    private String contents;
    private List<String>questions;
    private List<String>answers;
    public static MecoResponse fromMeco(Meco ledger) {
        return MecoResponse.builder()
                .contents(ledger.getContents())
                .questions(ledger.getQuestions())
                .answers(ledger.getAnswers())
                .build();
    }

}
