package com.memory.user;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RequiredArgsConstructor
@Service
@Getter
public class UserService {
    private final UserRepository userRepository;

    @Value("${secrete}")
    String secretKey;
    @PostConstruct
    public void init() {
        userRepository.save(User.builder()
                .userName("dd")
                .userId("dd")
                .userPw("sdd")
                .answers(new ArrayList<>(Arrays.asList("","","","","")))
                .role(UserRole.USER)
                .build());
        userRepository.save(User.builder()
                .userName("333dd")
                .userId("333")
                .userPw("333")
                .answers(new ArrayList<>(Arrays.asList("","","","","")))
                .role(UserRole.USER)
                .build());
        userRepository.save(User.builder()
                .userName("d555d")
                .userId("555")
                .userPw("5555")
                .answers(new ArrayList<>(Arrays.asList("","","","","")))
                .role(UserRole.ADMIN)
                .build());
    }
    public boolean isDuplicated (String userId){
        return userRepository.existsById(userId);
    }

    public void signUp(SignUpRequestDTO requestDTO) {
        userRepository.save(new User(requestDTO));
    }

    public List<String> login(LoginRequestDTO requestDTO) {
        Optional<User> byId = userRepository.findById(requestDTO.getUserId());
        if (byId.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "id " + requestDTO.getUserId() + " 가 없습니다");

        String userPw = byId.get().getUserPw();

        if (!userPw.equals(requestDTO.getUserPw())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 틀려요");
        }

        String accessToken = generateAccessToken(requestDTO.getUserId());
        String refreshToken = generateRefreshToken(requestDTO.getUserId());

        return new ArrayList<>(Arrays.asList(accessToken, refreshToken));
    }

    public String reissueToken(HttpServletRequest request) {
        //쿠키에서 토큰 꺼내기
        String jwtTokenCookie = getTokenCookie(request);
        System.out.println(jwtTokenCookie);
        if (jwtTokenCookie == null) {
            return null;
        }
        else if (getClaims(jwtTokenCookie, secretKey) == null){
            return null;
        }
        return generateAccessToken(getLoginId(jwtTokenCookie, secretKey));
    }
    public String getMyInfo(HttpServletRequest request) {
        User user = getLoginUserByUserId(getLoginId(getTokenFromHeader(request), secretKey));
        return user.getUserName();
    }
    public void signOut(HttpServletRequest request) {
        userRepository.deleteById(getLoginId(getTokenFromHeader(request), secretKey));
    }
    private String generateRefreshToken(String userId) {
        return generateToken(userId, 1000 * 60 * 60 * 24 * 14);
    }

    private String generateAccessToken(String userId) {
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

    private static String getTokenCookie(HttpServletRequest request) {
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


    public boolean isExpired(String token, String secretKey) {
        try{
            return getClaims(token, secretKey).getExpiration().before(new Date());
        } catch (ExpiredJwtException e){
            return true;
        }
    }
    public String getLoginId(String token, String secretKey) {
        return getClaims(token, secretKey).getSubject();
    }

    private static Claims getClaims(String token, String secretKey) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey.getBytes())
                    .parseClaimsJws(token).getBody();
        } catch (SignatureException e){
            return null;
        }
    }
    private static String getTokenFromHeader(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
    }

    public User getLoginUserByUserId(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저가 없어용 - 토큰"));
    }

    public ResponseCookie generateResponseCookie(String token, Integer maxAge){

        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true) // https 사용시
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();
    }

}
