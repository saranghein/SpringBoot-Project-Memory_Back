package com.memory.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignUpRequestDTO {
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Size(min = 2, max = 30, message = "이름은 2자 이상, 20자 이하로 입력해주세요.")
    private String userName;
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상, 20자 이하로 입력해주세요.")
    private String userId;
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 4, max = 20, message = "비밀번호는 4자 이상, 20자 이하로 입력해주세요.")
    private String userPw;
}
