package com.memory.ledger;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Arrays;
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
    public void testPostLedger() throws Exception {
        LedgerRequest request = new LedgerRequest();
        request.setEmotion("Happy");
        request.setEmotionCategory("Positive");
        request.setCategory("Work");
        request.setContents("Completed project");
        request.setTakedTime(2.5f);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime ledgerDate = LocalDate.parse("2024-07-21", formatter).atStartOfDay();//오늘 날짜로
        request.setLedgerDate(ledgerDate);

        String url = getBaseUrl() + "/api/v1/time-ledger/record";
        HttpEntity<LedgerRequest> entity = new HttpEntity<>(request, createHeaders("testToken"));

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        // JSON 출력 추가
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(jsonOutput);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("저장에 성공했습니다.");
    }

    @Test
    public void testGetLedgerByDate() throws Exception {
        String date = "2024-07-01";
        String url = getBaseUrl() + "/api/v1/time-ledger/records/date/" + date;
        HttpEntity<?> entity = new HttpEntity<>(createHeaders("testToken"));

        ResponseEntity<List<LedgerResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<LedgerResponse>>() {}
        );
        // JSON 출력 추가
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(jsonOutput);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testGetLedgerByRecordId() throws Exception{
        // LedgerRepository를 사용하여 저장된 Ledger ID를 얻음
        Long recordId = ledgerRepository.findAll().get(0).getRecordId();
        String url = getBaseUrl() + "/api/v1/time-ledger/records/" + recordId;
        HttpEntity<?> entity = new HttpEntity<>(createHeaders("testToken"));

        ResponseEntity<LedgerResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, LedgerResponse.class);
        // JSON 출력 추가
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(jsonOutput);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    @Test
    public void testGetContentsLedger() throws Exception{
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<Ledger> ledgers = Arrays.asList(
                Ledger.builder()
                        .ledgerDate(today)
                        .emotion("Happy")
                        .emotionCategory("Positive")
                        .category("Work")
                        .contents("Completed project")
                        .takedTime(2.5f)
                        .userId("testUser")
                        .build(),
                Ledger.builder()
                        .ledgerDate(today)
                        .emotion("Sad")
                        .emotionCategory("Negative")
                        .category("Personal")
                        .contents("Had a tough day")
                        .takedTime(1.0f)
                        .userId("testUser")
                        .build()
        );

        ledgerRepository.saveAll(ledgers);

        String url = getBaseUrl() + "/api/v1/time-ledger/today-records";
        HttpEntity<?> entity = new HttpEntity<>(createHeaders("testToken"));

        ResponseEntity<LedgerResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                LedgerResponse.class
        );

        LedgerResponse responseBody = response.getBody();
        // JSON 출력 추가
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseBody);
        System.out.println(jsonOutput);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getContentsList()).hasSize(2);
        assertThat(responseBody.getContentsList()).containsExactlyInAnyOrder("Completed project", "Had a tough day");

    }
    @Test
    public void testDeleteLedgerByRecordId() throws Exception{
        // LedgerRepository를 사용하여 저장된 Ledger ID를 얻음
        Long recordId = ledgerRepository.findAll().get(0).getRecordId();
        String url = getBaseUrl() + "/api/v1/time-ledger/records/" + recordId;
        HttpEntity<?> entity = new HttpEntity<>(createHeaders("testToken"));

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        // JSON 출력 추가
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(jsonOutput);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("레코드가 성공적으로 삭제되었습니다.");
    }
    @Test
    public void testGetStatistics() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfCurrentMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        List<Ledger> ledgers = Arrays.asList(
                Ledger.builder().ledgerDate(now).emotion("Happy").emotionCategory("긍정").category("Work").contents("Completed project").takedTime(2.5f).userId("testUser").build(),
                Ledger.builder().ledgerDate(now).emotion("Neutral").emotionCategory("중립").category("Work").contents("Meeting").takedTime(1.0f).userId("testUser").build(),
                Ledger.builder().ledgerDate(now).emotion("Sad").emotionCategory("부정").category("Work").contents("Had a tough day").takedTime(3.0f).userId("testUser").build(),
                Ledger.builder().ledgerDate(now.minusMonths(1)).emotion("Happy").emotionCategory("긍정").category("Work").contents("Completed project last month").takedTime(3.0f).userId("testUser").build(),
                Ledger.builder().ledgerDate(now.minusMonths(1)).emotion("Neutral").emotionCategory("중립").category("Work").contents("Meeting last month").takedTime(2.0f).userId("testUser").build(),
                Ledger.builder().ledgerDate(now.minusMonths(1)).emotion("Sad").emotionCategory("부정").category("Work").contents("Had a tough day last month").takedTime(1.0f).userId("testUser").build()
        );

        ledgerRepository.saveAll(ledgers);

        String url = getBaseUrl() + "/api/v1/time-ledger/statistics";
        HttpEntity<?> entity = new HttpEntity<>(createHeaders("testToken"));

        ResponseEntity<StatisticsResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                StatisticsResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        StatisticsResponse responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        // JSON 출력 추가
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseBody);
        System.out.println(jsonOutput);

        // Time spent assertions
        List<StatisticsResponse.TimeSpent> timeSpent = responseBody.getTimeSpent();
        assertThat(timeSpent).isNotEmpty();
        assertThat(timeSpent.get(0).getCategory()).isEqualTo("Work");
        assertThat(timeSpent.get(0).getHours()).isEqualTo(6.5f);  // 2.5f + 1.0f + 3.0f
        assertThat(timeSpent.get(0).getPercentage()).isGreaterThan(0);

        // Comparison with last month assertions
        StatisticsResponse.ComparisonWithLastMonth comparisonWithLastMonth = responseBody.getComparisonWithLastMonth();
        assertThat(comparisonWithLastMonth).isNotNull();
        assertThat(comparisonWithLastMonth.getPreviousCategory()).isEqualTo("Work");
        assertThat(comparisonWithLastMonth.getPreviousMonth()).isEqualTo(startOfCurrentMonth.minusMonths(1).getMonthValue());
        assertThat(comparisonWithLastMonth.getPreviousHours()).isEqualTo(6.0f);  // 3.0f + 2.0f + 1.0f
        assertThat(comparisonWithLastMonth.getCurrentCategory()).isEqualTo("Work");
        assertThat(comparisonWithLastMonth.getCurrentMonth()).isEqualTo(startOfCurrentMonth.getMonthValue());
        assertThat(comparisonWithLastMonth.getCurrentHours()).isEqualTo(6.5f);  // 2.5f + 1.0f + 3.0f

        // Emotions summary assertions
        List<StatisticsResponse.EmotionSummary> emotionsSummary = responseBody.getEmotionsSummary();
        assertThat(emotionsSummary).isNotEmpty();
        assertThat(emotionsSummary.stream().anyMatch(summary -> summary.getType().equals("긍정") && summary.getCount() == 1)).isTrue();
        assertThat(emotionsSummary.stream().anyMatch(summary -> summary.getType().equals("중립") && summary.getCount() == 1)).isTrue();
        assertThat(emotionsSummary.stream().anyMatch(summary -> summary.getType().equals("부정") && summary.getCount() == 1)).isTrue();

        // Positive emotions assertions
        List<StatisticsResponse.EmotionDetail> positiveEmotions = responseBody.getPositiveEmotions();
        assertThat(positiveEmotions).isNotEmpty();
        assertThat(positiveEmotions.stream().anyMatch(detail -> detail.getEmotions().equals("Happy") && detail.getCount() == 1)).isTrue();

        // Neutral emotions assertions
        List<StatisticsResponse.EmotionDetail> neutralEmotions = responseBody.getNeutralEmotions();
        assertThat(neutralEmotions).isNotEmpty();
        assertThat(neutralEmotions.stream().anyMatch(detail -> detail.getEmotions().equals("Neutral") && detail.getCount() == 1)).isTrue();

        // Negative emotions assertions
        List<StatisticsResponse.EmotionDetail> negativeEmotions = responseBody.getNegativeEmotions();
        assertThat(negativeEmotions).isNotEmpty();
        assertThat(negativeEmotions.stream().anyMatch(detail -> detail.getEmotions().equals("Sad") && detail.getCount() == 1)).isTrue();
    }

}
