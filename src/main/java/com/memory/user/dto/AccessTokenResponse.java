package com.memory.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AccessTokenResponse {
    private String accessToken;
    private String message;

    @Builder
    public AccessTokenResponse(String accessToken, String message) {
        this.accessToken = accessToken;
        this.message = message;
    }

    public static AccessTokenResponse of(String accessToken) {
        return AccessTokenResponse.builder().accessToken(accessToken).build();
    }

    public static AccessTokenResponse message(String message) {
        return AccessTokenResponse.builder().message(message).build();
    }
}
