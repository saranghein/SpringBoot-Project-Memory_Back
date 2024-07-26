package com.memory.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponseDTO {
    private String accessToken;
    private String userName;
    private String message;

    @Builder
    public LoginResponseDTO(String accessToken, String userName, String message) {
        this.accessToken = accessToken;
        this.userName = userName;
        this.message = message;
    }

    public static LoginResponseDTO of(String accessToken, String userName) {
        return LoginResponseDTO.builder().accessToken(accessToken).userName(userName).build();
    }

    public static LoginResponseDTO message(String message) {
        return LoginResponseDTO.builder().message(message).build();
    }
}
