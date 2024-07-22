package com.memory.ledger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

//jwt 우회
@Profile("test")
@Configuration
public class TestConfig {

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil() {
            @Override
            public boolean validateToken(String token) {
                return true; // 항상 유효한 토큰으로 처리
            }

            @Override
            public String extractUserId(String token) {
                return "testUser"; // 테스트용 사용자 ID 반환
            }
        };
    }
}
