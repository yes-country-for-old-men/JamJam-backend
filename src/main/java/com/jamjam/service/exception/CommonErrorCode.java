package com.jamjam.service.exception;

import com.jamjam.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {
    JSON_PROCESSING_ERROR(HttpStatus.BAD_REQUEST, "JSON 처리 중 오류가 발생했습니다.", "JSON_PROCESSING_ERROR"),
    OPENAI_API_ERROR(HttpStatus.BAD_GATEWAY, "OpenAI API 요청 중 오류가 발생했습니다.", "OPENAI_API_ERROR"),
    IMAGE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패", "IMAGE_UPLOAD_ERROR"),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 유저를 찾을 수 없습니다.", "USER_NOT_FOUND");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;

    CommonErrorCode(HttpStatus httpStatus, String message, String errorCode) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.errorCode = errorCode;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
