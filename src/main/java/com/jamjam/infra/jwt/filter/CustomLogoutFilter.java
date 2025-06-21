package com.jamjam.infra.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.jamjam.infra.jwt.application.JwtUtil;
import com.jamjam.infra.jwt.domain.repository.RefreshRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.scheduling.config.TaskExecutionOutcome.Status.SUCCESS;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;

public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public CustomLogoutFilter(JwtUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        doFilter((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        if (!requestURI.contains("/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        String refresh = null;
        Cookie[] cookies = request.getCookies();

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (jwtUtil.isTokenExpired(refresh)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        String type = jwtUtil.getType(refresh);

        if (!type.equals("refresh")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!refreshRepository.existsByRefresh(refresh)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        refreshRepository.deleteByRefresh(refresh);

        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        Map<String, Object> responseBody = new HashMap<>();
        Map<String, Object> innerResponse = new HashMap<>();

        responseBody.put("code", SUCCESS);
        responseBody.put("message", "요청이 성공적으로 처리되었습니다.");
        responseBody.put("content", innerResponse);

        ResponseDto<Void> responseDto = ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS);
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.print(new ObjectMapper().writeValueAsString(responseDto));

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
