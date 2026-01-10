package com.jamjam.chat.exception;

import com.jamjam.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ChatError implements ErrorCode {
    ACCESS_EXPIRED(HttpStatus.BAD_REQUEST,"토큰이 만료되었습니다.", "ACCESS_EXPIRED"),
    ROOM_NOT_FOUND(HttpStatus.BAD_REQUEST, "채팅방이 존재하지 않습니다." ,"ROOM_NOT_FOUND"),
    NOT_PARTICIPANT(HttpStatus.BAD_REQUEST, "해당 채팅방의 참가자가 아닙니다.", "NOT_PARTICIPANT"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다.", "USER_NOT_FOUND"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기는 10MB를 초과할 수 없습니다.", "FILE_SIZE_EXCEEDED"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다.", "INVALID_FILE_TYPE"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.", "FILE_UPLOAD_FAILED"),
    FILE_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "파일이 제공되지 않았습니다.", "FILE_NOT_PROVIDED");

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
