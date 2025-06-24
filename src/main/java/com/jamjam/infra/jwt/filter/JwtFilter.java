package com.jamjam.infra.jwt.filter;


import com.jamjam.infra.jwt.application.JwtUtil;
import com.jamjam.infra.jwt.dto.JwtUserDto;
import com.jamjam.infra.jwt.exception.JwtErrorCode;
import com.jamjam.infra.jwt.exception.JwtResponseUtil;
import com.jamjam.user.application.dto.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final String[] excludeUrls;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtFilter(JwtUtil jwtUtil, String[] excludeUrls) {
        this.jwtUtil = jwtUtil;
        this.excludeUrls = excludeUrls;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        System.out.println(Arrays.toString(excludeUrls));
        System.out.println(request.getRequestURI());
        String path = request.getRequestURI();
        return Arrays.stream(excludeUrls)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            if (!shouldNotFilter(request)) {
                logger.error("ACCESS TOKEN이 존재하지 않습니다.");
                JwtResponseUtil.sendErrorResponse(response, JwtErrorCode.ACCESS_INVALID);
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = token.substring(7);

        try {
            jwtUtil.isTokenExpired(accessToken);
            String type = jwtUtil.getType(accessToken);

            if (!"access".equals(type)) {
                logger.error("ACCESS TOKEN이 아닙니다.");
                JwtResponseUtil.sendErrorResponse(response, JwtErrorCode.ACCESS_INVALID);
                return;
            }

            Long userId = jwtUtil.getUserIdFromToken(accessToken);
            String role = jwtUtil.getUserRoleFromToken(accessToken);

            JwtUserDto user = JwtUserDto.builder()
                    .userId(userId)
                    .role(role)
                    .build();

            CustomUserDetails customUserDetails = new CustomUserDetails(user);

            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    customUserDetails, null, customUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.error("⏰ ACCESS TOKEN 만료: exp={}, uri={}",
                    e.getClaims().getExpiration(), request.getRequestURI(), e);
            JwtResponseUtil.sendErrorResponse(response, JwtErrorCode.ACCESS_EXPIRED);
        } catch (IllegalArgumentException e) {
            log.error("🚫 ACCESS TOKEN 파싱 오류: {}", accessToken, e);
            logger.error("ACCESS TOKEN의 인자가 잘못되었습니다.", e);
            JwtResponseUtil.sendErrorResponse(response, JwtErrorCode.ACCESS_INVALID);
        } catch (Exception e) {
            logger.error("ACCESS TOKEN 처리 중 오류가 발생했습니다.", e);
            JwtResponseUtil.sendErrorResponse(response, JwtErrorCode.ACCESS_INVALID);
        }
    }
}
