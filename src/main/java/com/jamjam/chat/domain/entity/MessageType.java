package com.jamjam.chat.domain.entity;

public enum MessageType {
    TEXT,
    IMAGE,
    FILE,
    REQUEST_FORM,       //의뢰서 송신
    REQUEST_PAYMENT,    //결제 요청
    PAYMENT_COMPLETED,  //결제 완료
    ORDER_CANCELLED,    //주문 취소
    WORK_COMPLETED,    //작업 완료
    SERVICE_INQUIRY     //서비스 문의
}
