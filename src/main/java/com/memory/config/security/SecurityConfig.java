package com.memory.config.security;

import com.memory.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @Bean
    public JwtTokenFilter jwtTokenFilter() {
        return new JwtTokenFilter(userService, jwtTokenUtil);
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.formLogin(AbstractHttpConfigurer::disable) // Disable form login
                .httpBasic(AbstractHttpConfigurer::disable) // Disable basic auth
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session management
                .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class) // Add JWT filter
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/v1/meta-questions", "/api/v1/myPage", "/api/v1/time-ledger/*", "/api/v1/meco/*").authenticated() // Secure endpoints
                        .anyRequest().permitAll()) // Allow all other requests
                .build();
    }

}
