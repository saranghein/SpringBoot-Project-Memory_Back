package com.memory.meco;

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

import java.util.Optional;
@Tag(name = "Meco", description = "메코의 질문 API")
@RestController
@RequestMapping("/api/v1/meco")
public class MecoController {
    private final MecoService mecoService;
    private final UserService userService;

    @Autowired
    public MecoController(MecoService mecoService, UserService userService) {
        this.mecoService = mecoService;
        this.userService = userService;
    }


    //질문 작성
    @Operation(summary = "질문 작성", description = "메코의 질문을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "저장에 성공했습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "해당 날짜는 이미 작성돼 있습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "날짜가 일치하지 않습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "저장에 실패했습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/questions")
    ResponseEntity<String>postQuestions(@RequestBody @Schema(description = "Meco Request Data", implementation = MecoRequest.class)MecoRequest mecoRequest, HttpServletRequest request) {
        try{
            //userId 검증
            String userId=userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);

            LocalDate todayDate = LocalDate.now();
            LocalDate requestDate = mecoRequest.getMecoDate();


            // 요청된 날짜와 오늘 날짜가 같다면
            if (todayDate.isEqual(requestDate)&&mecoService.getMecoByDateAndUserId(requestDate, user).isEmpty()) {
                Meco meco = mecoRequest.toMeco(user); // Ledger 객체 생성
                mecoService.saveMeco(meco);
                return new ResponseEntity<>("저장에 성공했습니다.", HttpStatus.CREATED);

            } else if (mecoService.getMecoByDateAndUserId(requestDate, user).isPresent()) {
                return new ResponseEntity<>("해당 날짜는 이미 작성돼 있습니다.", HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>("날짜가 일치하지 않습니다.", HttpStatus.CONFLICT);
            }
        }
        catch (Exception e) {
            return new ResponseEntity<>("저장에 실패했습니다.",HttpStatus.NOT_FOUND);
        }
    }

    //해당 날짜의 답변들 조회
    @Operation(summary = "해당 날짜의 답변들 조회", description = "해당 날짜의 메코 질문에 대한 답변들을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 조회했습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MecoResponse.class))),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없습니다.",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/questions/{date}")
    ResponseEntity<MecoResponse>getAnswersByDate(@PathVariable LocalDate date, HttpServletRequest request) {
        try {
            // userId 검증
            String userId = userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);

            Optional<MecoResponse> mecoAnswers = mecoService.getMecoByDateAndUserId(date, user);

            return mecoAnswers.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(new MecoResponse(), HttpStatus.OK));
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
