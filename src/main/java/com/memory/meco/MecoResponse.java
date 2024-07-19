package com.memory.meco;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
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
