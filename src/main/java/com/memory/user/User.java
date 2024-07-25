package com.memory.user;

import com.memory.user.dto.SignUpRequestDTO;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Getter
@NoArgsConstructor
public class User {
    @Id
    private String userId;
    private String userPw;
    private String userName;

    @Convert(converter = StringListConverter.class)
    private List<String> answers;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    public User(SignUpRequestDTO signUpRequestDTO) {
        this.userId = signUpRequestDTO.getUserId();
        this.userPw = signUpRequestDTO.getUserPw();
        this.userName = signUpRequestDTO.getUserName();
        this.answers = new ArrayList<>(Arrays.asList("","","","",""));
        this.role = UserRole.USER;
    }

    @Builder
    public User(String userId, String userPw, String userName, List<String> answers, UserRole role) {
        this.userId = userId;
        this.userPw = userPw;
        this.userName = userName;
        this.answers = answers;
        this.role = role;
    }
}
