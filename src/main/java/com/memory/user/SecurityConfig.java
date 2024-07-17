package com.memory.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@PropertySource("classpath:application.yml")

public class SecurityConfig {

    private final UserService userService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(new JwtTokenFilter(userService, userService.getSecretKey()), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests() // 밑에 requestMatchers 를 인가 확인
//                .requestMatchers("/api/v1/time-leger").authenticated()
//                .requestMatchers("/api/v1/meco").authenticated()
//                .requestMatchers("/api/v1/meta-questions").authenticated()
//                .requestMatchers("/api/v1/statistics").authenticated()
                .requestMatchers("/api/v1/test").authenticated()
                .requestMatchers("/jwt-login/admin/**").hasAuthority(UserRole.ADMIN.name())
                .anyRequest().permitAll()
                .and()
                .exceptionHandling()
                // 인증 실패
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException, IOException {
                        // jwt-api-login(api)에서 인증에 실패하면 error을 그대로 출력
                        // jwt-login(화면)에서 인증에 실패하면 에러 페이지로 redirect
                        if (!request.getRequestURI().contains("api")) {
                            response.sendRedirect("/jwt-login/authentication-fail");
                        }
                    }
                })
                // 인가 실패
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        if (!request.getRequestURI().contains("api")) {
                            response.sendRedirect("/jwt-login/authorization-fail");
                        }
                    }
                })
                .and().build();
    }

}
