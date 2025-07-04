package com.jamjam.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class PrepareOrderRequest {
    private String paymentUid;
    private BigDecimal price;
}
