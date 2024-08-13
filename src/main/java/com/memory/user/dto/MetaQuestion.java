package com.memory.user.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class MetaQuestion {
    private List<String> answers;

    @Builder
    @JsonCreator
    public MetaQuestion(@JsonProperty("answers") List<String> answers) {
        this.answers = answers;
    }

    public static MetaQuestion of(List<String> answers) {
        return MetaQuestion.builder().answers(answers).build();
    }
}
