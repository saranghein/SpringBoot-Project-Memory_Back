package com.memory.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @GetMapping("/check-duplication")
    public ResponseEntity<Boolean> checkDuplication(@RequestParam String userId) {
        return ResponseEntity.ok(userService.isDuplicated(userId));
    }
    @PostMapping("/signUp")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequestDTO requestDTO){
        userService.signUp(requestDTO);
        return ResponseEntity.ok("회원가입에 성공했습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponseDTO> login(@RequestBody LoginRequestDTO requestDTO){
        List<String> tokens = userService.login(requestDTO);
        ResponseCookie refreshTokenCookie = userService.generateResponseCookie(tokens.get(0),21 * 24 * 60 * 60);

//        String accessToken = tokens.get(0);
//        String refreshToken = tokens.get(1);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(AccessTokenResponseDTO.of(tokens.get(1)));
    }
    @PostMapping("/accessToken")
    public ResponseEntity<AccessTokenResponseDTO> reissueToken(HttpServletRequest request){
//        if (userService.reissueToken(request) == null){
//            return null;
//        }
//        return ResponseEntity.ok(AccessTokenResponseDTO.of(userService.reissueToken(request)));
        return ResponseEntity.of(Optional.ofNullable(userService.reissueToken(request))
                .map(AccessTokenResponseDTO::of));
    }
    @GetMapping("/myPage")
    public ResponseEntity<MyPageResponseDTO> getMyInfo(HttpServletRequest request) {
        return ResponseEntity.ok(MyPageResponseDTO.of(userService.getMyInfo(request)));
    }
    @GetMapping("/logout")
    public ResponseEntity<String> logout(){
        ResponseCookie cookie = userService.generateResponseCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("로그아웃 완료");
    }
    @DeleteMapping("/account")
    public ResponseEntity<String> signOut(HttpServletRequest request){
        userService.signOut(request);
        return ResponseEntity.ok("삭제 완료 했습니다.");
    }
}
