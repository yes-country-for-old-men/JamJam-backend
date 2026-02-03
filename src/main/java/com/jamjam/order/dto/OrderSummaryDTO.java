package com.jamjam.order.dto;

import com.jamjam.order.domain.entity.OrderEntity;
import com.jamjam.order.domain.entity.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrderSummaryDTO(
        Long orderId,
        String title,
        String client,
        LocalDateTime orderedAt,
        OrderStatus orderStatus
) {
    public static OrderSummaryDTO from(OrderEntity entity) {
        return new OrderSummaryDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getClient().getNickname(),
                entity.getOrderedAt(),
                entity.getOrderStatus()
        );
    }
}
