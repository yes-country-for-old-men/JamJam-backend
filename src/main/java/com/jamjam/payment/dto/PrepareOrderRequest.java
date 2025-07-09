package com.jamjam.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PrepareOrderRequest {
    private String paymentUid;
    private BigDecimal price;
}
