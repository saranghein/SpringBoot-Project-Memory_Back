package com.memory.user;

import com.memory.user.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "User", description = "유저 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    //중복체크
    @Operation(summary = "중복 체크", description = "유저 ID 중복 체크를 수행합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "중복 체크 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))})
    @GetMapping("/check-duplication")
    public ResponseEntity<Boolean> checkDuplication(@RequestParam @Schema(description = "유저 ID", example = "user123") String userId) {
        try {
            return ResponseEntity.ok(userService.isDuplicated(userId));
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id는 null이 아닙니다", e);
        }
    }

    //회원가입
    @Operation(summary = "회원가입", description = "유저 회원가입을 수행합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "회원가입 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), @ApiResponse(responseCode = "404", description = "회원가입 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @PostMapping("/signUp")
    public ResponseEntity<String> signUp(@Valid @RequestBody @Schema(description = "회원가입 요청 데이터", implementation = SignUpRequest.class) SignUpRequest requestDTO) {
        try {
            userService.signUp(requestDTO);
            return ResponseEntity.ok("회원가입에 성공했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원가입에 실패했습니다: " + e.getMessage());
        }
    }

    //로그인
    @Operation(summary = "로그인", description = "유저 로그인을 수행합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))), @ApiResponse(responseCode = "400", description = "아이디나 비밀번호가 틀렸습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))), @ApiResponse(responseCode = "404", description = "로그인 실패", content = @Content(mediaType = "application/json"))})
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Schema(description = "로그인 요청 데이터", implementation = LoginRequest.class) LoginRequest requestDTO) {
        try {
            List<String> tokens = userService.login(requestDTO);
            ResponseCookie refreshTokenCookie = userService.generateResponseCookie(tokens.get(1), 21 * 24 * 60 * 60);
//        String accessToken = tokens.get(0);
//        String refreshToken = tokens.get(1);
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString()).body(LoginResponse.of(tokens.get(0), userService.findNameById(requestDTO.getUserId())));
        } catch (ResponseStatusException ResponseStatusException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(LoginResponse.message("아이디나 비밀번호가 틀렸어요"));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //accessToken 재발급
    @Operation(summary = "액세스 토큰 재발급", description = "새로운 액세스 토큰을 재발급받습니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "재발급 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccessTokenResponse.class))), @ApiResponse(responseCode = "415", description = "Refresh token이 만료되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccessTokenResponse.class))), @ApiResponse(responseCode = "404", description = "재발급 실패", content = @Content(mediaType = "application/json"))})
    @PostMapping("/accessToken")
    public ResponseEntity<AccessTokenResponse> reissueToken(HttpServletRequest request) {
        try {
            String newAccessToken = userService.reissueToken(request);
            return ResponseEntity.ok(AccessTokenResponse.of(newAccessToken));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(AccessTokenResponse.message("refreshToken 이 만료됐어요"));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //meta-question get&patch
    @Operation(summary = "메타 질문 조회", description = "메타 질문의 답변을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetaQuestion.class))), @ApiResponse(responseCode = "404", description = "조회 실패", content = @Content(mediaType = "application/json"))})
    @GetMapping("/meta-questions")
    public ResponseEntity<MetaQuestion> getMetaAnswers(HttpServletRequest request) {
        try {
            return ResponseEntity.ok(MetaQuestion.of(userService.getAnswers(request)));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "메타 질문 수정", description = "메타 질문의 답변을 수정합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), @ApiResponse(responseCode = "404", description = "수정 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
    @PatchMapping("/meta-questions")
    public ResponseEntity<String> patchMetaAnswers(@RequestBody @Schema(description = "메타 질문 수정 데이터", implementation = MetaQuestion.class) MetaQuestion metaQuestionDTO, HttpServletRequest request) {
        try {
            userService.updateAnswers(metaQuestionDTO.getAnswers(), request);
            return ResponseEntity.ok("수정 완료");
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    //로그아웃
    @Operation(summary = "로그아웃", description = "유저 로그아웃을 수행합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), @ApiResponse(responseCode = "404", description = "로그아웃 실패", content = @Content(mediaType = "application/json"))})
    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        try {
            ResponseCookie cookie = userService.generateResponseCookie("", 0);
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body("로그아웃 완료");
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    //회원 탈퇴
    @Operation(summary = "회원 탈퇴", description = "유저 회원 탈퇴를 수행합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "회원 탈퇴 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), @ApiResponse(responseCode = "404", description = "회원 탈퇴 실패", content = @Content(mediaType = "application/json"))})
    @DeleteMapping("/account")
    public ResponseEntity<String> signOut(HttpServletRequest request) {
        try {
            userService.signOut(request);
            return ResponseEntity.ok("삭제 완료 했습니다.");
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ControllerAdvice // signup시 id나 비밀번호 조건 만족 x / 중복 체크시 userid null 일 때 예외처리 클래스
    public static class GlobalExceptionHandler {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
            Map<String, String> errors = new HashMap<>();
            ex.getBindingResult().getAllErrors().forEach((error) -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getReason());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
