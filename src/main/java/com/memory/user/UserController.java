package com.memory.user;

import com.memory.user.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    //중복체크
    @GetMapping("/check-duplication")
    public ResponseEntity<Boolean> checkDuplication(@RequestParam String userId) {
        return ResponseEntity.ok(userService.isDuplicated(userId));
    }
    //회원가입
    @PostMapping("/signUp")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequestDTO requestDTO) {
        userService.signUp(requestDTO);
        return ResponseEntity.ok("회원가입에 성공했습니다.");
    }
    //로그인
    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponseDTO> login(@RequestBody LoginRequestDTO requestDTO) {
        List<String> tokens = userService.login(requestDTO);
        System.out.println(33);
        ResponseCookie refreshTokenCookie = userService.generateResponseCookie(tokens.get(1), 21 * 24 * 60 * 60);
//        String accessToken = tokens.get(0);
//        String refreshToken = tokens.get(1);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(AccessTokenResponseDTO.of(tokens.get(0)));
    }
    //accessToken 재발급
    @PostMapping("/accessToken")
    public ResponseEntity<AccessTokenResponseDTO> reissueToken(HttpServletRequest request) {
//        if (userService.reissueToken(request) == null){
//            return null;
//        }
//        return ResponseEntity.ok(AccessTokenResponseDTO.of(userService.reissueToken(request)));
        return ResponseEntity.of(Optional.ofNullable(userService.reissueToken(request))
                .map(AccessTokenResponseDTO::of));
    }
    //meta-question get&patch
    @GetMapping("/meta-questions")
    public ResponseEntity<MetaQuestionDTO> getMetaAnswers(HttpServletRequest request) {
        return ResponseEntity.ok(MetaQuestionDTO.of(userService.getAnswers(request)));
    }
    @PatchMapping("/meta-questions")
    public ResponseEntity<String> patchMetaAnswers(@RequestBody MetaQuestionDTO metaQuestionDTO, HttpServletRequest request) {
        userService.updateAnswers(metaQuestionDTO.getAnswers(), request);
        return ResponseEntity.ok("수정 완료");
    }
    //마이페이지에 올라갈 유저 정보 get
    @GetMapping("/myPage")
    public ResponseEntity<MyPageResponseDTO> getMyInfo(HttpServletRequest request) {
        return ResponseEntity.ok(MyPageResponseDTO.of(userService.getMyInfo(request)));
    }
    //로그아웃
    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        ResponseCookie cookie = userService.generateResponseCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("로그아웃 완료");
    }
    //회원 탈퇴
    @DeleteMapping("/account")
    public ResponseEntity<String> signOut(HttpServletRequest request) {
        userService.signOut(request);
        return ResponseEntity.ok("삭제 완료 했습니다.");
    }
}
