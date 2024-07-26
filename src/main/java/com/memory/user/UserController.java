package com.memory.user;

import com.memory.user.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        try {
            userService.signUp(requestDTO);
            return ResponseEntity.ok("회원가입에 성공했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원가입에 실패했습니다: " + e.getMessage());
        }
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO requestDTO) {
        try {
            List<String> tokens = userService.login(requestDTO);
            ResponseCookie refreshTokenCookie = userService.generateResponseCookie(tokens.get(1), 21 * 24 * 60 * 60);
//        String accessToken = tokens.get(0);
//        String refreshToken = tokens.get(1);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(LoginResponseDTO.of(tokens.get(0), userService.findNameById(requestDTO.getUserId())));
        } catch (ResponseStatusException ResponseStatusException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(LoginResponseDTO.message("아이디나 비밀번호가 틀렸어요"));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //accessToken 재발급
    @PostMapping("/accessToken")
    public ResponseEntity<AccessTokenResponseDTO> reissueToken(HttpServletRequest request) {
        try {
            String newAccessToken = userService.reissueToken(request);
            return ResponseEntity.ok(AccessTokenResponseDTO.of(newAccessToken));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(AccessTokenResponseDTO.message("refreshToken 이 만료됐어요"));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //meta-question get&patch
    @GetMapping("/meta-questions")
    public ResponseEntity<MetaQuestionDTO> getMetaAnswers(HttpServletRequest request) {
        try {
            return ResponseEntity.ok(MetaQuestionDTO.of(userService.getAnswers(request)));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/meta-questions")
    public ResponseEntity<String> patchMetaAnswers(@RequestBody MetaQuestionDTO metaQuestionDTO, HttpServletRequest request) {
        try {
            userService.updateAnswers(metaQuestionDTO.getAnswers(), request);
            return ResponseEntity.ok("수정 완료");
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    //로그아웃
    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        try {
            ResponseCookie cookie = userService.generateResponseCookie("", 0);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body("로그아웃 완료");
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    //회원 탈퇴
    @DeleteMapping("/account")
    public ResponseEntity<String> signOut(HttpServletRequest request) {
        try {
            userService.signOut(request);
            return ResponseEntity.ok("삭제 완료 했습니다.");
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
