package com.jamjam.order.exception;

import com.jamjam.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OrderError implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다.", "USER_NOT_FOUND"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주문을 찾을 수 없습니다.", "ORDER_NOT_FOUND"),
    SERVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 서비스를 찾을 수 없습니다.", "SERVICE_NOT_FOUND"),
    SELF_ORDER_NOT_ALLOWED(HttpStatus.FORBIDDEN, "자신의 서비스에 주문을 신청할 수 없습니다.", "SELF_ORDER_NOT_ALLOWED"),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "참고자료 업로드 실패", "FILE_UPLOAD_ERROR"),
    JSON_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 변환 실패", "JSON_PROCESSING_ERROR"),
    CANNOT_ACCEPT_ORDER(HttpStatus.BAD_REQUEST, "주문을 수락할 수 없는 상태입니다.", "CANNOT_ACCEPT_ORDER"),
    CANNOT_COMPLETE_ORDER(HttpStatus.BAD_REQUEST, "제공자가 직접 서비스를 구매 확정 처리할 수 없습니다.", "CANNOT_COMPLETE_ORDER"),
    CANNOT_CANCEL_AT_THIS_STATUS(HttpStatus.BAD_REQUEST, "취소할 수 있는 주문 상태가 아닙니다.", "CANNOT_CANCEL_AT_THIS_STATUS"),
    CANNOT_REQUEST_PAYMENT(HttpStatus.BAD_REQUEST, "결제 요청할 수 있는 주문 상태가 아닙니다.", "CANNOT_REQUEST_PAYMENT"),
    CANNOT_PROCESS_PAYMENT(HttpStatus.BAD_REQUEST, "결제할 수 있는 주문 상태가 아닙니다.", "CANNOT_PROCESS_PAYMENT"),
    FORBIDDEN_CHANGE_ORDER_STATUS(HttpStatus.FORBIDDEN, "주문 상태를 수정할 권한이 없습니다.", "FORBIDDEN_CHANGE_ORDER_STATUS"),
    FORBIDDEN_REQUEST_PAYMENT(HttpStatus.FORBIDDEN, "해당 주문에 결제 요청할 권한이 없습니다.", "FORBIDDEN_REQUEST_PAYMENT"),
    FORBIDDEN_PROCESS_PAYMENT(HttpStatus.FORBIDDEN, "해당 주문에 결제할 권한이 없습니다.", "FORBIDDEN_PROCESS_PAYMENT"),
    IS_NOT_PROVIDER(HttpStatus.BAD_REQUEST, "제공자 유저가 아닙니다.", "IS_NOT_PROVIDER"),
    IS_NOT_CLIENT(HttpStatus.BAD_REQUEST, "제공자 유저가 아닙니다.", "IS_NOT_CLIENT"),
    UNKNOWN_USER_ROLE(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 역할입니다.", "UNKNOWN_USER_ROLE"),
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
