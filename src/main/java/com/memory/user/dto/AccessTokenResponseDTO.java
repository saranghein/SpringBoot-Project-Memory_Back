package com.memory.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AccessTokenResponseDTO {
    private String accessToken;
    @Builder
    public AccessTokenResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }

    public static AccessTokenResponseDTO of(String accessToken){
        return AccessTokenResponseDTO.builder().accessToken(accessToken).build();
    }
}
