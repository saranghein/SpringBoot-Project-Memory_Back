package com.memory.ledger;

import com.memory.MemoryApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {MemoryApplication.class, LedgerControllerTest.TestConfig.class})
@ActiveProfiles("test") // 테스트 프로파일 활성화
public class LedgerControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Configuration
    static class TestConfig {
        @Bean
        public TestRestTemplate restTemplate(RestTemplateBuilder builder) {
            return new TestRestTemplate(builder);
        }

        @Bean
        public LedgerController ledgerController(LedgerService ledgerService) {
            return new LedgerController(ledgerService);
        }
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }
    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
    @BeforeEach
    public void setUp() {
        ledgerRepository.deleteAll(); // 기존 데이터를 삭제

        // 테스트 데이터를 초기화
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime ledgerDate = LocalDate.parse("2024-07-01", formatter).atStartOfDay();

        Ledger ledger = Ledger.builder()
                .emotion("Happy")
                .emotionCategory("Positive")
                .category("Work")
                .contents("Completed project")
                .takedTime(2.5f)
                .userId("testUser")
                .ledgerDate(ledgerDate)
                .build();
        ledgerRepository.save(ledger);
    }

    @Test
    public void testPostLedger() {
        LedgerRequest request = new LedgerRequest();
        request.setEmotion("Happy");
        request.setEmotionCategory("Positive");
        request.setCategory("Work");
        request.setContents("Completed project");
        request.setTakedTime(2.5f);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime ledgerDate = LocalDate.parse("2024-07-18", formatter).atStartOfDay();
        request.setLedgerDate(ledgerDate);

        String url = getBaseUrl() + "/api/v1/time-ledger/record";
        HttpEntity<LedgerRequest> entity = new HttpEntity<>(request, createHeaders("testToken"));

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("저장에 성공했습니다.");
    }

    @Test
    public void testGetLedgerByDate() {
        String date = "2024-07-01";
        String url = getBaseUrl() + "/api/v1/time-ledger/records/date/" + date;
        HttpEntity<?> entity = new HttpEntity<>(createHeaders("testToken"));

        ResponseEntity<List<LedgerResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<LedgerResponse>>() {}
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testGetLedgerByRecordId() {
        // LedgerRepository를 사용하여 저장된 Ledger ID를 얻음
        Long recordId = ledgerRepository.findAll().get(0).getRecordId();
        String url = getBaseUrl() + "/api/v1/time-ledger/records/" + recordId;
        HttpEntity<?> entity = new HttpEntity<>(createHeaders("testToken"));

        ResponseEntity<LedgerResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, LedgerResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testDeleteLedgerByRecordId() {
        // LedgerRepository를 사용하여 저장된 Ledger ID를 얻음
        Long recordId = ledgerRepository.findAll().get(0).getRecordId();
        String url = getBaseUrl() + "/api/v1/time-ledger/records/" + recordId;
        HttpEntity<?> entity = new HttpEntity<>(createHeaders("testToken"));

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("레코드가 성공적으로 삭제되었습니다.");
    }
}
