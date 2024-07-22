package com.memory.ledger;

import com.memory.user.User;
import com.memory.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/time-ledger")
public class LedgerController {

    private final LedgerService ledgerService;
    private final UserService userService;
//    private final JwtUtil jwtUtil;
//    private final boolean isTestEnvironment;

    //실제 코드
    @Autowired
    public LedgerController(LedgerService ledgerService, UserService userService) {
        this.ledgerService = ledgerService;
        this.userService = userService;
        //this.jwtUtil = jwtUtil;
        //this.isTestEnvironment = false; // Default value

    }

    // Test 용 (JWT 우회)
//    public LedgerController(LedgerService ledgerService) {
//        this.ledgerService = ledgerService;
//        this.jwtUtil = null;
//        this.isTestEnvironment = true; // Test environment
//    }

//    private ResponseEntity<String> validateTokenAndGetUserId(HttpServletRequest request) {
//        try {
//            if (isTestEnvironment) {// Test에서 jwt 우회 위함
//                return new ResponseEntity<>("testUser", HttpStatus.OK);
//            }
//
//            String authorizationHeader = request.getHeader("Authorization");
//            System.out.println("Authorization Header: " + authorizationHeader);
//
//            if (authorizationHeader != null && authorizationHeader.equals("Bearer testToken")) {
//                // Test 환경에서 JWT 우회
//                return new ResponseEntity<>("testUser", HttpStatus.OK);
//            }
//            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
////                return new ResponseEntity<>("testUser", HttpStatus.OK);
//
//                return new ResponseEntity<>("JWT 토큰이 필요합니다.", HttpStatus.UNAUTHORIZED);
//            }
//
//            String token = authorizationHeader.substring(7);
//            if (!jwtUtil.validateToken(token)) {
//                return new ResponseEntity<>("JWT 토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
//            }
//
//            //userId 추출
//            String userId = jwtUtil.extractUserId(token);
//            return new ResponseEntity<>(userId, HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>("JWT 토큰 처리 중 오류가 발생했습니다.", HttpStatus.UNAUTHORIZED);
//        }
//    }

    private LocalDateTime dateFormat(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter).atStartOfDay();
    }

    //시간 가계부 작성
    @PostMapping("/record")
    public ResponseEntity<String> postLedger(@RequestBody LedgerRequest ledgerRequest, HttpServletRequest request) {
        try {
            //ledgerRequest의 userId 검증
            String userId=userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);

//            ResponseEntity<String> userIdResponse = validateTokenAndGetUserId(request);
//            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
//                return userIdResponse;
//            }
//            String userId = userIdResponse.getBody();

            //LocalDateTime -> LocalDate 변환 필요
            LocalDate todayDate = LocalDate.now();
            LocalDate requestDate = ledgerRequest.getLedgerDate();


            // 요청된 날짜와 오늘 날짜가 같다면
            if (todayDate.isEqual(requestDate)) {
                Ledger ledger = ledgerRequest.toLedger(user); // Ledger 객체 생성
                ledgerService.saveLedger(ledger);
                return new ResponseEntity<>("저장에 성공했습니다.", HttpStatus.CREATED);

            } else {
                return new ResponseEntity<>("날짜가 일치하지 않습니다.", HttpStatus.CONFLICT);

            }
        }catch (Exception e){

            return new ResponseEntity<>("저장에 실패했습니다.", HttpStatus.NOT_FOUND);
        }
    }

    //특정 날짜의 시간 가계부 가져오기
    @GetMapping("/records/date/{date}")
    public ResponseEntity<List<LedgerResponse>> getLedgerByDate(@PathVariable LocalDate date, HttpServletRequest request) {
        try {
            // userId 검증
            String userId=userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);

//            ResponseEntity<String> userIdResponse = validateTokenAndGetUserId(request);
//            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
//                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//            }
//            String userId = userIdResponse.getBody();

            List<LedgerResponse> ledgers = ledgerService.getLedgerByDateAndUserId(date, user);
            return new ResponseEntity<>(ledgers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //특정 record의 시간 가계부 가져오기
    @GetMapping("/records/{recordId}")
    public ResponseEntity<LedgerResponse> getLedgerByRecordId(@PathVariable Long recordId,HttpServletRequest request) {
        // userId 검증
        String userId=userService.getLoginIdFromRequest(request);
        User user = userService.getLoginUserByUserId(userId);

//        ResponseEntity<String> userIdResponse = validateTokenAndGetUserId(request);
//        if (userIdResponse.getStatusCode() != HttpStatus.OK) {
//            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//        }
//        String userId = userIdResponse.getBody();

        Optional<LedgerResponse> ledgerResponse = ledgerService.getLedgerByRecordIdAndUserId(recordId, user);
        return ledgerResponse.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    //오늘의 record의 contents만 보여줌
    @GetMapping("/today-records")
    public ResponseEntity<LedgerResponse>getContentsLedger(HttpServletRequest request) {
        try {
            // userId 검증
            String userId=userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);

//            ResponseEntity<String> userIdResponse = validateTokenAndGetUserId(request);
//            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
//                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//            }
//            String userId = userIdResponse.getBody();

            LocalDate localDate = LocalDate.now();

            List<Ledger> ledgers = ledgerService.getContentsByUserIdAndDate(user, localDate);
            LedgerResponse response = LedgerResponse.fromLedgerList(ledgers);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //특정 record의 시간 가계부 삭제
    @DeleteMapping("/records/{recordId}")
    public ResponseEntity<String> deleteLedgerByRecordId(@PathVariable Long recordId,HttpServletRequest request) {
        try {
            // userId 검증
            String userId=userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);

//            ResponseEntity<String> userIdResponse = validateTokenAndGetUserId(request);
//            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
//                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//            }
//            String userId = userIdResponse.getBody();

            Optional<LedgerResponse> ledgerResponse = ledgerService.getLedgerByRecordIdAndUserId(recordId, user);
            // recordId에 대한 데이터가 있는지 검사
            if (ledgerResponse.isEmpty()) {
                return new ResponseEntity<>("레코드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
            }

            ledgerService.deleteLedgerByRecordId(recordId);
            return new ResponseEntity<>("레코드가 성공적으로 삭제되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("레코드 삭제 중 오류가 발생했습니다.", HttpStatus.NOT_FOUND);
        }
    }

    //ledger로 통계값 계산해서 가져옴
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics(HttpServletRequest request) {
        try {
            //userId 검증
            String userId=userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);


//            ResponseEntity<String> userIdResponse = validateTokenAndGetUserId(request);
//            if (userIdResponse.getStatusCode() != HttpStatus.OK) {
//                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//            }
//            String userId = userIdResponse.getBody();

            StatisticsResponse statistics = ledgerService.getStatistics(user);
            return new ResponseEntity<>(statistics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
