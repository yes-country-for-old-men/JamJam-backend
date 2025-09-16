package com.jamjam.order.dto;

import com.jamjam.order.domain.entity.OrderEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrderSummaryDTO(
        Long orderId,
        String title,
        String client,
        LocalDateTime orderedAt
) {
    public static OrderSummaryDTO from(OrderEntity entity) {
        return new OrderSummaryDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getClient().getNickname(),
                entity.getOrderedAt()
        );
    }
}
