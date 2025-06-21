package com.jamjam.infra.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.global.exception.ApiException;
import com.jamjam.infra.jwt.application.JwtUtil;
import com.jamjam.infra.jwt.domain.entity.RefreshEntity;
import com.jamjam.infra.jwt.domain.repository.RefreshRepository;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.exception.UserError;
import com.jamjam.global.dto.ResponseDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RefreshRepository refreshRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, RefreshRepository refreshRepository) {
        RequestMatcher requestMatcher = request -> {
            String uri = request.getRequestURI();
            String method = request.getMethod();
            return "/api/user/login".equals(uri) && "POST".equals(method);
        };
        super.setRequiresAuthenticationRequestMatcher(requestMatcher);
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Value("${spring.cors.allowed_origins}")
    private String allowedOrigins;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            Map jsonRequest = objectMapper.readValue(request.getInputStream(), Map.class);
            String userLoginId = (String) jsonRequest.get("loginId");
            String password = (String) jsonRequest.get("password");

            if (userLoginId == null || password == null) {
                throw new ApiException(UserError.LOGIN_INPUT_EMPTY) {
                };
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userLoginId, password);
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new ApiException(UserError.VERIFICATION_NOT_MATCH);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication) throws IOException {
        Long userId = extractUserId(authentication);
        String role = extractUserRole(authentication);

        String accessToken = jwtUtil.generateToken("access", userId, role, 60 * 60 * 1000L);
        String refreshToken = jwtUtil.generateToken("refresh", userId, role, 60 * 60 * 24 * 1000L);

        addRefreshEntity(userId, refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, toCookie(refreshToken).toString());

        response.setHeader("Access-Control-Allow-Origin", allowedOrigins);
        response.setHeader("Access-Control-Allow-Credentials", "true");

        Map<String, Object> body = Map.of(
                "accessToken", accessToken,
                "tokenType",   "Bearer"
        );

        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }

    private void addRefreshEntity(Long userId, String refresh) {
        LocalDateTime date = jwtUtil.getExpiration(refresh);

        RefreshEntity refreshEntity = RefreshEntity.builder()
                .userId(userId)
                .refresh(refresh)
                .expires(date)
                .build();

        refreshRepository.save(refreshEntity);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        ResponseDto<Void> errorBody = ResponseDto.ofFailure("LOGIN_FAILED", failed.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), errorBody);
    }

    protected ResponseCookie toCookie(String refreshToken) {
        return ResponseCookie
                .from("refresh", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    private Long extractUserId(Authentication authentication) {
        return ((CustomUserDetails) authentication.getPrincipal()).getUserId();
    }

    private String extractUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new ApiException(UserError.VERIFICATION_NOT_FOUND));
    }
}
