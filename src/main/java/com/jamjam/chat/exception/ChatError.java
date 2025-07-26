package com.jamjam.chat.exception;

import com.jamjam.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ChatError implements ErrorCode {
    ACCESS_EXPIRED(HttpStatus.BAD_REQUEST,"토큰이 만료되었습니다.", "ACCESS_EXPIRED"),
    ROOM_NOT_FOUND(HttpStatus.BAD_REQUEST, "채팅방이 존재하지 않습니다." ,"ROOM_NOT_FOUND"),
    NOT_PARTICIPANT(HttpStatus.BAD_REQUEST, "해당 채팅방의 참가자가 아닙니다.", "NOT_PARTICIPANT"),;

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
