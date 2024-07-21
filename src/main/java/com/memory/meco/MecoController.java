package com.memory.meco;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/meco")
public class MecoController {
    private final MecoService mecoService;
    private final JwtUtil jwtUtil;
    private final boolean isTestEnvironment;

    //실제 코드
    @Autowired
    public MecoController(MecoService mecoService, JwtUtil jwtUtil) {
        this.mecoService = mecoService;
        this.jwtUtil = null;
        this.isTestEnvironment = false; // Default value

    }


    // Constructor for test environment
    public MecoController(MecoService mecoService) {
        this.mecoService = mecoService;
        this.jwtUtil = null;
        this.isTestEnvironment = true; // Test environment
    }

    //추후 별도 클래스로 분리 예정
    private ResponseEntity<String> validateTokenAndGetUserId(HttpServletRequest request) {
        try {
            if (isTestEnvironment) {// Test에서 jwt 우회 위함
                return new ResponseEntity<>("testUser", HttpStatus.OK);
            }

            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.equals("Bearer testToken")) {
                // Test 환경에서 JWT 우회
                return new ResponseEntity<>("testUser", HttpStatus.OK);
            }
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return new ResponseEntity<>("JWT 토큰이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            String token = authorizationHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return new ResponseEntity<>("JWT 토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
            }

            //userId 추출
            String userId = jwtUtil.extractUserId(token);
            return new ResponseEntity<>(userId, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("JWT 토큰 처리 중 오류가 발생했습니다.", HttpStatus.UNAUTHORIZED);
        }
    }
    private LocalDateTime dateFormat(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter).atStartOfDay();
    }
    //질문 작성
    @PostMapping("/questions")
    ResponseEntity<String>postQuestions(@RequestBody MecoRequest mecoRequest, HttpServletRequest request) {
        try{
            //mecoRequest의 userId 검증
            ResponseEntity<String> userIdResponse = validateTokenAndGetUserId(request);
            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
                return userIdResponse;
            }
            String userId = userIdResponse.getBody();

            //LocalDateTime -> LocalDate 변환 필요
            LocalDateTime todayDate = dateFormat(LocalDateTime.now().toLocalDate().toString());
            LocalDateTime requestDate = mecoRequest.getMecoDate();


            // 요청된 날짜와 오늘 날짜가 같다면
            if (todayDate.isEqual(requestDate)) {
                Meco meco = mecoRequest.toMeco(userId); // Ledger 객체 생성
                mecoService.saveMeco(meco);
                return new ResponseEntity<>("저장에 성공했습니다.", HttpStatus.CREATED);

            } else {
                return new ResponseEntity<>("날짜가 일치하지 않습니다.", HttpStatus.CONFLICT);

            }
        }
        catch (Exception e) {
            return new ResponseEntity<>("저장에 실패했습니다.",HttpStatus.NOT_FOUND);
        }
    }

    //해당 날짜의 답변들 조회
    @GetMapping("questions/{date}")
    ResponseEntity<MecoResponse>getAnswersByDate(@PathVariable String date, HttpServletRequest request) {
            // userId 검증
            ResponseEntity<String> userIdResponse = validateTokenAndGetUserId(request);
            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            String userId = userIdResponse.getBody();

            LocalDateTime localDate = dateFormat(date);

            Optional<MecoResponse> mecoAnswers = mecoService.getMecoByDateAndUserId(localDate, userId);
            return mecoAnswers.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }


    //작성했던 ledger의 contents들 불러오기
    @GetMapping("today-records")
    ResponseEntity<List<MecoResponse>>getRecords(HttpServletRequest request){
        return null;//ledger의 api에 작성필요 (같은 서비스를 요청하면 문제 발생하기 때문
    }
}
