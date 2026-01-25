package com.jamjam.chat.presentation.dto.req;

public record PaymentReq(
        Long orderId,
        Integer price
) {
}
