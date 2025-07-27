package com.jamjam.payment.exception;

import com.jamjam.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum PaymentError implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다.", "USER_NOT_FOUND"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Merchant ID에 해당하는 주문을 찾을 수 없습니다.", "ORDER_NOT_FOUND"),
    SERVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "Service ID에 해당하는 서비스를 찾을 수 없습니다.", "SERVICE_NOT_FOUND"),
    GET_PAYMENT_FAILED(HttpStatus.BAD_GATEWAY, "Payment Detail API 호출에 실패했습니다.", "GET_PAYMENT_FAILED"),
    REFUND_FAILED(HttpStatus.BAD_GATEWAY, "결제 취소 API 호출에 실패했습니다.", "REFUND_FAILED"),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "결제 정보가 일치하지 않아 환불 처리되었습니다.", "PAYMENT_FAILED");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;

    PaymentError(HttpStatus httpStatus, String message, String errorCode) {
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
