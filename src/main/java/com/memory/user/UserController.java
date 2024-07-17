package com.memory.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok("저장에 성공했습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponseDTO> login(@RequestBody LoginRequestDTO requestDTO){
        return ResponseEntity.ok(userService.login(requestDTO));
    }
    @PostMapping("/accessToken")
    public ResponseEntity<AccessTokenResponseDTO> reissueToken(){
        return ResponseEntity.ok(userService.reissueToken());
    }

}
