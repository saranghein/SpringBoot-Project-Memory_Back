package com.memory.meco;

import com.memory.user.User;
import com.memory.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import java.util.Optional;

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


    //질문 작성(작성 날짜 unique)
    @PostMapping("/questions")
    ResponseEntity<String>postQuestions(@RequestBody MecoRequest mecoRequest, HttpServletRequest request) {
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

    //해당 날짜의 답변들 조회(날짜 unique하게 처리 필요)
    @GetMapping("/questions/{date}")
    ResponseEntity<MecoResponse>getAnswersByDate(@PathVariable LocalDate date, HttpServletRequest request) {
            // userId 검증
            String userId=userService.getLoginIdFromRequest(request);
            User user = userService.getLoginUserByUserId(userId);

            Optional<MecoResponse> mecoAnswers = mecoService.getMecoByDateAndUserId(date, user);
            return mecoAnswers.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }


}
