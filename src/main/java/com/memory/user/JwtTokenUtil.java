package com.memory.user;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

@Getter
@Component // 왜 이게 없음 오류?
public class JwtTokenUtil {

    @Value("${secrete}")
    private String secretKey;

    public boolean isTokenInvalid(String token, String secretKey) {
        try {
            Claims claims = getClaims(token, secretKey);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            // JWT 관련 예외 처리
            System.out.println("JWT processing error: " + e.getMessage());
            return true;
        } catch (Exception e) {
            // 기타 예외 처리
            System.out.println("Unexpected error: " + e.getMessage());
            return true;
        }
    }
    public String getTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        // 쿠키가 없는 경우 빈 문자열을 반환
        if (cookies == null) {
            return null;
        }
        return Objects.requireNonNull(Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .findFirst()
                .orElse(null)).getValue();
    }
    public String getTokenFromHeader(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
    }
    public static Claims getClaims(String token, String secretKey) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token).getBody();
    }
    public String generateRefreshToken(String userId) {
        return generateToken(userId, 1000 * 60 * 60 * 24 * 14);
    }

    public String generateAccessToken(String userId) {
        return generateToken(userId, 1000 * 60 * 60);
    }

    public String generateToken(String userId, long expirationTime) {
        Claims claims = Jwts.claims(); // jwt 라이브러리 claim 메서드 claim 을 만드는 것으로 보임
        claims.setSubject(userId); // 입력받은 loginid을 claim에 추가
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis())) // 발생 시간
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 만료 시간인듯
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes()) // 서명하는 부분 서명은 안전한 토큰이란 걸 나타내고 이 서명을 할 수 있게 해주느 것이 비밀키임
                .compact();
    }

}
