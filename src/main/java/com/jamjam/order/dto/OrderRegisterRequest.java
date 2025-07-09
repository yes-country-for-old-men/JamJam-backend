package com.jamjam.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class OrderRegisterRequest {
    private String title;
    private LocalDate deadline;
    private String description;
    private BigDecimal price;
    private Long serviceId;
}
