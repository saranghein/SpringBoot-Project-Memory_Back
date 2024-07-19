package com.memory.user;

import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class test {

    @GetMapping("/api/v1/test")
    public ResponseEntity<String> doTest(){
        return ResponseEntity.ok("test");
    }
}
