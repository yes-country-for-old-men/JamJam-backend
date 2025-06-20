package com.jamjam.infra.jwt.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        JwtErrorCode errorCode = JwtErrorCode.ACCESS_INVALID;

        Object exception = request.getAttribute("exception");

        if (exception instanceof JwtErrorCode) {
            errorCode = (JwtErrorCode) exception;
        }

        JwtResponseUtil.sendErrorResponse(response, errorCode);
    }
}