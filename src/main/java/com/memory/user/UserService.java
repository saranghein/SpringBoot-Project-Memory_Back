package com.memory.user;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

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

    public AccessTokenResponseDTO login(LoginRequestDTO requestDTO) {
        Optional<User> byId = userRepository.findById(requestDTO.getUserId());
        if (byId.isEmpty()) throw new NoSuchElementException("id " + requestDTO.getUserId() + " 가 없습니다");

        String userPw = byId.get().getUserPw();
        if (!userPw.equals(requestDTO.getUserPw())) {
            return null;
        }
        return new AccessTokenResponseDTO(createToken(requestDTO.getUserId()));
    }
    public String createToken(String userId) {
        long expireTimeMs = 1000 * 60 * 60;     // Token 유효 시간 = 60분

        Claims claims = Jwts.claims(); // jwt 라이브러리 claim 메서드 claim 을 만드는 것으로 보임
        claims.put("userId", userId); // 입력받은 loginid을 claim에 추가

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis())) // 발생 시간
                .setExpiration(new Date(System.currentTimeMillis() + expireTimeMs)) // 만료 시간인듯
                .signWith(SignatureAlgorithm.HS256, secretKey) // 서명하는 부분 서명은 안전한 토큰이란 걸 나타내고 이 서명을 할 수 있게 해주느 것이 비밀키임
                .compact();
    }

    public AccessTokenResponseDTO reissueToken() {
        String token = "2";
        return new AccessTokenResponseDTO(token);
    }

    public User getLoginUserByUserId(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("유저가 없어용 - 토큰"));
    }

    public String getLoginId(String token, String secretKey) {
        return getClaims(token, secretKey).getId();
    }

    public boolean isExpired(String token, String secretKey) {
        return getClaims(token, secretKey).getExpiration().before(new Date());
    }

    private static Claims getClaims(String token, String secretKey) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJwt(token).getBody();
    }

}
