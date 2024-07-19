package com.memory.user;

import io.jsonwebtoken.Header;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
@RequiredArgsConstructor
public class TestFilter extends OncePerRequestFilter {
    private final UserService userService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().equals("/mypage")) {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//            System.out.println(authorizationHeader);
            if (authorizationHeader == null) {
                // 클라이언트에게 JSON 형식의 에러 메시지 응답
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"토큰이 보내지지 않았어용\"}");
                return;
            }
            String token = authorizationHeader.split(" ")[1];
            // 전송받은 Jwt Token이 만료되었으면 => 다음 필터 진행(인증 X)
            if(userService.isExpired(token, userService.secretKey)) {
                System.out.println(222);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"token is expired\"}");
                return;
            }

            // Jwt Token에서 loginId 추출
            String userId = userService.getLoginId(token, userService.secretKey);

            // 추출한 loginId로 User 찾아오기
            User loginUser = userService.getLoginUserByUserId(userId);

            // loginUser 정보로 UsernamePasswordAuthenticationToken 발급
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginUser.getUserId(), null, List.of(new SimpleGrantedAuthority(loginUser.getRole().name())));
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 권한 부여
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        }
        filterChain.doFilter(request, response);
    }
}
