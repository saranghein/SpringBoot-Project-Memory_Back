package com.memory.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// OncePerRequestFilter : 매번 들어갈 때 마다 체크 해주는 필터
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().equals("/api/v1/meta-questions") |
                request.getRequestURI().equals("/api/v1/myPage") |
                request.getRequestURI().equals("/api/v1/logout") |
                request.getRequestURI().equals("/api/v1/account") |
                request.getRequestURI().equals("/api/v1/meta-questions") |
                request.getRequestURI().equals("/api/v1/myPage") |
                request.getRequestURI().equals("/api/v1/time-ledger/*") |
                request.getRequestURI().equals("/api/v1/meco/*")
        ) {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//            System.out.println(authorizationHeader);
            if (authorizationHeader == null) {
                // 클라이언트에게 JSON 형식의 에러 메시지 응답
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"token is not sent\"}");
                return;
            }
            String token = authorizationHeader.split(" ")[1];
            // 전송받은 Jwt Token이 만료되었으면 => 다음 필터 진행(인증 X)
            if(userService.isTokenInvalid(token, jwtTokenUtil.getSecretKey())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"token is invalid\"}");
                return;
            }
            // Jwt Token에서 loginId 추출
            String userId = userService.getLoginId(token, jwtTokenUtil.getSecretKey());

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
        else filterChain.doFilter(request,response);
    }

}

