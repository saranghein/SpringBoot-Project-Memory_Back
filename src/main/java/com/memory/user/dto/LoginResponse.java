package com.memory.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponse {
    private String accessToken;
    private String userName;
    private String message;

    @Builder
    public LoginResponse(String accessToken, String userName, String message) {
        this.accessToken = accessToken;
        this.userName = userName;
        this.message = message;
    }

    public static LoginResponse of(String accessToken, String userName) {
        return LoginResponse.builder().accessToken(accessToken).userName(userName).build();
    }

    public static LoginResponse message(String message) {
        return LoginResponse.builder().message(message).build();
    }
}
