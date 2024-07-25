package com.memory.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponseDTO {
    private String accessToken;
    private String userName;
    @Builder
    public LoginResponseDTO(String accessToken, String userName) {
        this.accessToken = accessToken;
        this.userName = userName;
    }
    public static LoginResponseDTO of(String accessToken, String userName){
        return LoginResponseDTO.builder().accessToken(accessToken).userName(userName).build();
    }
}
