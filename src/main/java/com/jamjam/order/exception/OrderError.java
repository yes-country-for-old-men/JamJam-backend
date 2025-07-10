package com.jamjam.order.exception;

import com.jamjam.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OrderError implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다.", "USER_NOT_FOUND"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문을 찾을 수 없습니다.", "ORDER_NOT_FOUND"),
    SERVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 서비스를 찾을 수 없습니다.", "SERVICE_NOT_FOUND"),
    IMAGE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패", "IMAGE_UPLOAD_ERROR"),
    CANNOT_ACCEPT_ORDER(HttpStatus.BAD_REQUEST, "주문을 수락할 수 없는 상태입니다.", "CANNOT_ACCEPT_ORDER"),
    FORBIDDEN_CHANGE_ORDER_STATUS(HttpStatus.FORBIDDEN, "주문 상태를 수정할 권한이 없습니다.", "FORBIDDEN_CHANGE_ORDER_STATUS"),
    UNKNOWN_STATUS(HttpStatus.BAD_REQUEST, "알 수 없는 주문 상태입니다.", "UNKNOWN_STATUS"),
    CREDIT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "크레딧 부족으로 주문을 진행할 수 없습니다.", "CREDIT_NOT_ENOUGH");

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
