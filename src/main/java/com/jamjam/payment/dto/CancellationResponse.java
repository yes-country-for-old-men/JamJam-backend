package com.jamjam.payment.dto;

import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
public class CancellationResponse {
    private String status;
    private Integer totalAmount;
    private String reason;
}
