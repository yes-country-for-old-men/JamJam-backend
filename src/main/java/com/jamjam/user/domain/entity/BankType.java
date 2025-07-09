package com.jamjam.user.domain.entity;

import com.jamjam.global.exception.ApiException;
import com.jamjam.user.exception.UserError;

public enum BankType {
    KOREA("001", "한국은행"),
    INDUSTRIAL("002", "산업은행"),
    IBK("003", "기업은행"),
    KB("004", "국민은행"),
    SUHYUP("007", "수협은행"),
    EXIM("008", "수출입은행"),
    NH("011", "농협은행"),
    SC("023", "SC제일은행"),
    CITI("027", "한국씨티은행"),
    BUSAN("032", "부산은행"),
    DAEGU("031", "대구은행"),
    GWANGJU("034", "광주은행"),
    JEJU("035", "제주은행"),
    JEONBUK("037", "전북은행"),
    GYEONGNAM("039", "경남은행"),
    POST("071", "우체국"),
    HANA("080", "하나은행"),
    SHINHAN("088", "신한은행"),
    KBANK("089", "K뱅크"),
    KAKAO("090", "카카오뱅크"),
    TOSS("092", "토스뱅크");

    private final String code;
    private final String name;

    BankType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() { return code; }
    public String getName() { return name; }

    public static BankType fromCode(String code) {
        for (BankType type : values()) {
            if (type.code.equals(code)) return type;
        }
        throw new ApiException(UserError.BANK_NOT_FOUND);
    }
}
