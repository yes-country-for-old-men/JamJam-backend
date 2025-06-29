package com.jamjam.chat.exception;

import com.jamjam.global.exception.ApiException;
import com.jamjam.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ChatError implements ErrorCode {
    ACCESS_EXPIRED(HttpStatus.BAD_REQUEST,"ACCESS_EXPIRED", "토큰이 만료되었습니다.");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;

    ChatError(final HttpStatus httpStatus, final String message, final String errorCode) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.errorCode = errorCode;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

}
