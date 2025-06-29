package com.jamjam.service.exception;

import com.jamjam.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {
    JSON_PROCESSING_ERROR(HttpStatus.BAD_REQUEST, "JSON 처리 중 오류가 발생했습니다.", "JSON_PROCESSING_ERROR"),
    OPENAI_API_ERROR(HttpStatus.BAD_GATEWAY, "OpenAI API 요청 중 오류가 발생했습니다.", "OPENAI_API_ERROR"),
    IMAGE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패", "IMAGE_UPLOAD_ERROR"),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 유저를 찾을 수 없습니다.", "USER_NOT_FOUND"),
    SERVICE_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 서비스를 찾을 수 없습니다.", "SERVICE_NOT_FOUND"),
    S3_IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 이미지를 S3에서 찾을 수 없습니다.", "S3_IMAGE_NOT_FOUND"),
    FORBIDDEN_DELETE(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.", "FORBIDDEN_DELETE"),
    FORBIDDEN_MODIFY(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.", "FORBIDDEN_MODIFY"),
    NO_AUTH_WRITE(HttpStatus.FORBIDDEN, "서비스 등록 권한이 없습니다.", "NO_AUTH_WRITE");

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
