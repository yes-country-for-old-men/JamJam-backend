package com.jamjam.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompletePaymentRequest {
    private String paymentUid;
}
