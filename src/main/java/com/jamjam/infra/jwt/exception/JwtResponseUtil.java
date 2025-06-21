package com.jamjam.infra.jwt.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import com.jamjam.global.dto.ResponseDto;

public class JwtResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void sendErrorResponse(HttpServletResponse response, JwtErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ResponseDto<Void> errorResponse = ResponseDto.ofFailure(errorCode.getCode(), errorCode.getMessage());
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
