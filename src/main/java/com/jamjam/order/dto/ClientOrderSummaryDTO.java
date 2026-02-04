package com.jamjam.order.dto;

import com.jamjam.order.domain.entity.OrderEntity;
import com.jamjam.order.domain.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClientOrderSummaryDTO(
        Long orderId,
        String title,
        Long serviceId,
        String serviceName,
        String thumbnailUrl,
        LocalDateTime orderedAt,
        OrderStatus orderStatus,
        BigDecimal price
) {
    public static ClientOrderSummaryDTO from(OrderEntity entity) {
        OrderStatus currentOrderStatus = entity.getOrderStatus();

        BigDecimal price = null;
        if (currentOrderStatus != OrderStatus.REQUESTED && currentOrderStatus != OrderStatus.CANCELLED) {
            price = entity.getPrice();
        }

        return new ClientOrderSummaryDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getService().getId(),
                entity.getService().getServiceName(),
                entity.getService().getThumbnail(),
                entity.getOrderedAt(),
                entity.getOrderStatus(),
                price
        );
    }
}
