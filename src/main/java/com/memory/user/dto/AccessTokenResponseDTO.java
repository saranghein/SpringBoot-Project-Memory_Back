package com.memory.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AccessTokenResponseDTO {
    private String accessToken;
    private String message;

    @Builder
    public AccessTokenResponseDTO(String accessToken, String message) {
        this.accessToken = accessToken;
        this.message = message;
    }

    public static AccessTokenResponseDTO of(String accessToken) {
        return AccessTokenResponseDTO.builder().accessToken(accessToken).build();
    }

    public static AccessTokenResponseDTO message(String message) {
        return AccessTokenResponseDTO.builder().message(message).build();
    }
}
