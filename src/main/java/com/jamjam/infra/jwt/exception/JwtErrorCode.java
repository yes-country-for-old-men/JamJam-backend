package com.jamjam.infra.jwt.exception;

public enum JwtErrorCode {
    ACCESS_EXPIRED("ACCESS_EXPIRED", "토큰이 만료되었습니다."),
    ACCESS_INVALID("ACCESS_INVALID", "유효하지 않은 토큰입니다.");

    private final String code;
    private final String message;

    JwtErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}