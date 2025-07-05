package com.jamjam.order.exception;

import com.jamjam.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OrderError implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 유저를 찾을 수 없습니다.", "USER_NOT_FOUND"),
    SERVICE_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 서비스를 찾을 수 없습니다.", "SERVICE_NOT_FOUND"),
    IMAGE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패", "IMAGE_UPLOAD_ERROR");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;

    OrderError(HttpStatus httpStatus, String message, String errorCode) {
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
