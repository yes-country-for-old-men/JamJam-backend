package com.jamjam.notify.exception;

import com.jamjam.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum NotifyError implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 유저를 찾을 수 없습니다.", "USER_NOT_FOUND"),
    CANNOT_SEND_MESSAGE(HttpStatus.BAD_GATEWAY, "FCM 메시지 전송 실패하였습니다.", "CANNOT_SEND_MESSAGE"),
    INVALID_FCM_TOKEN(HttpStatus.NOT_FOUND, "FCM 토큰에 매핑된 디바이스를 찾을 수 없습니다.", "INVALID_FCM_TOKEN"),
    NOT_FOUND_FCM_TOKEN(HttpStatus.NOT_FOUND, "FCM 토큰을 찾을 수 없습니다.", "NOT_FOUND_FCM_TOKEN");


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