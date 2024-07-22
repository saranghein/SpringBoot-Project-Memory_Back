package com.memory.meco;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memory.MemoryApplication;
import com.memory.user.UserService;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {MemoryApplication.class, MecoControllerTest.TestConfig.class})
@ActiveProfiles("test")
public class MecoControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MecoRepository mecoRepository;

    @Configuration
    static class TestConfig {
        @Bean
        public TestRestTemplate restTemplate(RestTemplateBuilder builder) {
            return new TestRestTemplate(builder);
        }

        @Bean
        public MecoController mecoController(MecoService mecoService, UserService userService) {
//            return new MecoController(mecoService, new JwtUtil()); // JwtUtil 빈 추가
            return new MecoController(mecoService, userService);
        }

        @Bean
        public MecoService mecoService(MecoRepository mecoRepository) {
            return new MecoService(mecoRepository);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return new JwtUtil();
        }
    }

    @BeforeEach
    public void setUp() {
        mecoRepository.deleteAll(); // 기존 데이터를 삭제합니다.
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    @Test
    public void testPostQuestions() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime mecoDate = LocalDate.parse("2024-07-21", formatter).atStartOfDay();

        MecoRequest request =new MecoRequest();
        request.setMecoDate(mecoDate);
        request.setContents("Some contents");
        request.setQuestions(Arrays.asList("Question 1", "Question 2", "Question 3"));
        request.setAnswers(Arrays.asList("Answer 1", "Answer 2", "Answer 3"));
                

        String url = getBaseUrl() + "/api/v1/meco/questions";
        HttpEntity<MecoRequest> entity = new HttpEntity<>(request, createHeaders("testToken"));

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        // JSON 출력 추가
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(jsonOutput);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("저장에 성공했습니다.");
    }

    @Test
    public void testGetAnswersByDate() throws Exception{
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime mecoDate = LocalDate.parse("2024-07-21", formatter).atStartOfDay();


        Meco meco = Meco.builder()
                .mecoDate(mecoDate)
                .contents("Some contents")
                .userId("testUser")
                .questions(Arrays.asList("Question 1", "Question 2", "Question 3"))
                .answers(Arrays.asList("Answer 1", "Answer 2", "Answer 3"))
                .build();

        mecoRepository.save(meco);

        String formattedDate = "2024-07-21";
        String url = getBaseUrl() + "/api/v1/meco/questions/" + formattedDate;
        HttpEntity<?> entity = new HttpEntity<>(createHeaders("testToken"));

        ResponseEntity<MecoResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<MecoResponse>() {}
        );
        MecoResponse responseBody = response.getBody();

        // JSON 출력 추가
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(jsonOutput);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getQuestions()).containsExactly("Question 1", "Question 2", "Question 3");
        assertThat(responseBody.getAnswers()).containsExactly("Answer 1", "Answer 2", "Answer 3");
    }
}