package com.memory.user.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class MetaQuestionDTO {
    private List<String> answers;

    @Builder
    @JsonCreator
    public MetaQuestionDTO(@JsonProperty("answers") List<String> answers) {
        this.answers = answers;
    }

    public static MetaQuestionDTO of(List<String> answers) {
        return MetaQuestionDTO.builder().answers(answers).build();
    }
}
