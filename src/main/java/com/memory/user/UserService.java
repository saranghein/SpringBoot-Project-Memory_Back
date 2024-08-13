package com.memory.user;

import com.memory.config.security.JwtTokenUtil;
import com.memory.user.dto.LoginRequest;
import com.memory.user.dto.SignUpRequest;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @PostConstruct
    public void init() {
        userRepository.save(User.builder()
                .userName("dd")
                .userId("dd")
                .userPw("sdd")
                .answers(new ArrayList<>(Arrays.asList("", "", "", "", "")))
                .role(UserRole.USER)
                .build());
        userRepository.save(User.builder()
                .userName("333dd")
                .userId("333")
                .userPw("333")
                .answers(new ArrayList<>(Arrays.asList("", "", "", "", "")))
                .role(UserRole.USER)
                .build());
        userRepository.save(User.builder()
                .userName("d555d")
                .userId("555")
                .userPw("5555")
                .answers(new ArrayList<>(Arrays.asList("", "", "", "", "")))
                .role(UserRole.ADMIN)
                .build());
    }

    public boolean isDuplicated(String userId) {
        if (userId.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id가 비어있습니다.");
        }
        return userRepository.existsById(userId);
    }

    public void signUp(SignUpRequest requestDTO) {
        userRepository.save(new User(requestDTO));
    }

    public List<String> login(LoginRequest requestDTO) {
        Optional<User> byId = userRepository.findById(requestDTO.getUserId());
        if (byId.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id " + requestDTO.getUserId() + " 가 없습니다");

        String userPw = byId.get().getUserPw();

        if (!userPw.equals(requestDTO.getUserPw())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호가 틀려요");
        }

        String accessToken = jwtTokenUtil.generateAccessToken(requestDTO.getUserId());
        String refreshToken = jwtTokenUtil.generateRefreshToken(requestDTO.getUserId());

        return new ArrayList<>(Arrays.asList(accessToken, refreshToken));
    }

    public String reissueToken(HttpServletRequest request) {
        //쿠키에서 토큰 꺼내기
        String jwtTokenCookie = jwtTokenUtil.getTokenCookie(request);
//        System.out.println(jwtTokenCookie);
        if (jwtTokenCookie == null | isTokenInvalid(jwtTokenCookie, jwtTokenUtil.getSecretKey())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "refreshToken 이 잘못됐습니다.");
        }
        return jwtTokenUtil.generateAccessToken(getLoginId(jwtTokenCookie, jwtTokenUtil.getSecretKey()));
    }

    public boolean isTokenInvalid(String token, String secretKey) {
        return jwtTokenUtil.isTokenInvalid(token, secretKey);
    }

    public void signOut(HttpServletRequest request) {
        userRepository.deleteById(getLoginId(jwtTokenUtil.getTokenFromHeader(request), jwtTokenUtil.getSecretKey()));
    }

    public List<String> getAnswers(HttpServletRequest request) {
        String userId = getLoginId(jwtTokenUtil.getTokenFromHeader(request), jwtTokenUtil.getSecretKey());
        return userRepository.findById(userId).get().getAnswers();
    }

    @Transactional
    public void updateAnswers(List<String> answers, HttpServletRequest request) {
        String userId = getLoginId(jwtTokenUtil.getTokenFromHeader(request), jwtTokenUtil.getSecretKey());
        userRepository.updateAnswers(userId, answers);
    }

    public String getLoginId(String token, String secretKey) {
        return jwtTokenUtil.getClaims(token, secretKey).getSubject();
    }

    public User getLoginUserByUserId(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저가 없어용 - 토큰"));
    }

    public ResponseCookie generateResponseCookie(String token, Integer maxAge) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true) // https 사용시
                .path("/")
                .maxAge(maxAge)
                .sameSite("None")
                .build();
    }

    public String getLoginIdFromRequest(HttpServletRequest request) {
        String token = jwtTokenUtil.getTokenFromHeader(request);
        return getLoginId(token, jwtTokenUtil.getSecretKey());
    }

    public String findNameById(String userId) {
        Optional<User> loginUser = userRepository.findById(userId);
        return loginUser.map(User::getUserName).orElse(null);
    }
}
