package com.memory.ledger;

import com.memory.user.User;
import com.memory.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
@Tag(name = "Ledger", description = "시간가계부 API")
@RestController
@RequestMapping("/api/v1/time-ledger")
public class LedgerController {

    private final LedgerService ledgerService;
    private final UserService userService;

    @Autowired
    public LedgerController(LedgerService ledgerService, UserService userService) {
        this.ledgerService = ledgerService;
        this.userService = userService;
    }


    //시간 가계부 작성
    @Operation(summary = "시간 가계부 작성", description = "시간 가계부를 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "저장에 성공했습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "날짜가 일치하지 않습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "저장에 실패했습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/record")
    public ResponseEntity<String> postLedger(@RequestBody @Schema(description = "Ledger Request Data", implementation = LedgerRequest.class)LedgerRequest ledgerRequest, HttpServletRequest request) {
        try {
            //ledgerRequest의 userId 검증
            String userId=userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);

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
    @Operation(summary = "특정 날짜의 시간 가계부 가져오기", description = "특정 날짜의 시간 가계부를 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 가져왔습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LedgerResponse.class))),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없습니다.",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/records/date/{date}")
    public ResponseEntity<List<LedgerResponse>> getLedgerByDate(@PathVariable LocalDate date, HttpServletRequest request) {
        try {
            // userId 검증
            String userId=userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);

            List<LedgerResponse> ledgers = ledgerService.getLedgerByDateAndUserId(date, user);
            return new ResponseEntity<>(ledgers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //특정 record의 시간 가계부 가져오기
    @Operation(summary = "특정 record의 시간 가계부 가져오기", description = "특정 record의 시간 가계부를 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 가져왔습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LedgerResponse.class))),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없습니다.",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/records/{recordId}")
    public ResponseEntity<LedgerResponse> getLedgerByRecordId(@PathVariable Long recordId,HttpServletRequest request) {
        // userId 검증
        String userId=userService.getLoginIdFromRequest(request);
        User user = userService.getLoginUserByUserId(userId);

        Optional<LedgerResponse> ledgerResponse = ledgerService.getLedgerByRecordIdAndUserId(recordId, user);
        return ledgerResponse.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    //오늘의 record의 contents만 보여줌
    @Operation(summary = "오늘의 record의 contents만 보여줌", description = "오늘의 record의 contents를 보여줍니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 가져왔습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LedgerResponse.class))),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없습니다.",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/today-records")
    public ResponseEntity<LedgerResponse>getContentsLedger(HttpServletRequest request) {
        try {
            // userId 검증
            String userId=userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);

            LocalDate localDate = LocalDate.now();

            List<Ledger> ledgers = ledgerService.getContentsByUserIdAndDate(user, localDate);
            LedgerResponse response = LedgerResponse.fromLedgerList(ledgers);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //특정 record의 시간 가계부 삭제
    @Operation(summary = "특정 record의 시간 가계부 삭제", description = "특정 record의 시간 가계부를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "레코드가 성공적으로 삭제되었습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "레코드를 찾을 수 없습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "레코드 삭제 중 오류가 발생했습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @DeleteMapping("/records/{recordId}")
    public ResponseEntity<String> deleteLedgerByRecordId(@PathVariable Long recordId,HttpServletRequest request) {
        try {
            // userId 검증
            String userId=userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);

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
    @Operation(summary = "ledger로 통계값 계산해서 가져옴", description = "ledger로 통계값을 계산하여 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 가져왔습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StatisticsResponse.class))),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없습니다.",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics(HttpServletRequest request) {
        try {
            //userId 검증
            String userId=userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);

            StatisticsResponse statistics = ledgerService.getStatistics(user);
            return new ResponseEntity<>(statistics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
