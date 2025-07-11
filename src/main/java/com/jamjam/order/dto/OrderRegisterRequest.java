package com.jamjam.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderRegisterRequest {
    private String title;
    private LocalDateTime deadline;
    private String description;
    private BigDecimal price;
    private Long serviceId;
}
