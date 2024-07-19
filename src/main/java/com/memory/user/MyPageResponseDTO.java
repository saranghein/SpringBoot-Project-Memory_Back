package com.memory.user;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MyPageResponseDTO {
    private String userName;
    @Builder
    public MyPageResponseDTO(String userName) {
        this.userName = userName;
    }

    public static MyPageResponseDTO of(String userName){
       return MyPageResponseDTO.builder().userName(userName).build();
    }
}
