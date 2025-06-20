package com.jamjam.infra.jwt.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JwtResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void sendErrorResponse(HttpServletResponse response, JwtErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("errorCode", errorCode.getCode());
        errorResponse.put("message", errorCode.getMessage());

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
