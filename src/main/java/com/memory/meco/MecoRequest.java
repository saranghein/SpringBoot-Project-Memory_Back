package com.memory.meco;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class MecoRequest {
    private LocalDateTime mecoDate;
    private String contents;
    private List<String>questions;
    private List<String>answers;

    public Meco toMeco(String userId) {
        return Meco.builder()
                .mecoDate(this.mecoDate)
                .contents(this.contents)
                .userId(userId)
                .questions(this.questions)
                .answers(this.answers)
                .build();
    }
}
