package com.jamjam.order.dto;

import com.jamjam.order.domain.entity.OrderEntity;
import com.jamjam.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;

public record ProviderOrderSummaryDTO(
        Long orderId,
        String title,
        String client,
        LocalDateTime orderedAt,
        OrderStatus orderStatus
) {
    public static ProviderOrderSummaryDTO from(OrderEntity entity) {
        return new ProviderOrderSummaryDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getClient().getNickname(),
                entity.getOrderedAt(),
                entity.getOrderStatus()
        );
    }
}
