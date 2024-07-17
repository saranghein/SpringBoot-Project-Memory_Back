package com.memory.user;

import lombok.Getter;

@Getter
public class AccessTokenResponseDTO {
    private String accessToken;
    public AccessTokenResponseDTO(String token) {
        this.accessToken = token;
    }
}
