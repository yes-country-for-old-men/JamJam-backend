package com.jamjam.notify;

import com.jamjam.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum NotifyError implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 유저를 찾을 수 없습니다.", "USER_NOT_FOUND");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;

    NotifyError(HttpStatus httpStatus, String message, String errorCode) {
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