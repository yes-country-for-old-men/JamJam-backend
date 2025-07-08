package com.jamjam.order.dto;

import com.jamjam.order.domain.entity.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderStatusRequest {
    private Long orderId;
    private OrderStatus orderStatus;
    private String cancelReason;
}
