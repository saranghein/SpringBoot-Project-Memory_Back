package com.memory.meco;

import com.memory.user.User;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class MecoRequest {
    private LocalDate mecoDate;
    private String contents;
    private List<String>questions;
    private List<String>answers;

    public Meco toMeco(User user) {
        return Meco.builder()
                .mecoDate(this.mecoDate)
                .contents(this.contents)
                .user(user)
                .questions(this.questions)
                .answers(this.answers)
                .build();
    }
}
