package com.memory.meco;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값을 가진 필드는 JSON에 포함되지 않음
public class MecoResponse {
    @Schema(description = "메코 내용", example = "오늘의 메코 내용")
    private String contents;

    @Schema(description = "질문 목록",example = "[\"기분이 어땠나요?\",\"그 행동의 결과는 어땠나요?\", \"다음에는 어떻게 해결할 건가요?\"]")
    private List<String> questions;

    @Schema(description = "답변 목록",example = "[\"싸워서 마음이 아팠어요\",\"나의 주먹이 날라갔어요\",\"주먹보다 말로 풀려고 할 거에요\"]")
    private List<String> answers;

    public static MecoResponse fromMeco(Meco ledger) {
        return MecoResponse.builder()
                .contents(ledger.getContents())
                .questions(ledger.getQuestions())
                .answers(ledger.getAnswers())
                .build();
    }

}

