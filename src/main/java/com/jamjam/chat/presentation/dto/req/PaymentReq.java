package com.jamjam.chat.presentation.dto.req;

public record PaymentReq(
        Long roomId,
        Long orderId,
        Integer price
) {
}
