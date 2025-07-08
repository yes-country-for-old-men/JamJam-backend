package com.jamjam.order.dto;

import com.jamjam.order.domain.entity.OrderEntity;

import java.time.LocalDate;

public record OrderSummaryDTO(
        Long orderId,
        String title,
        String client,
        LocalDate deadline
) {
    public static OrderSummaryDTO from(OrderEntity entity) {
        return new OrderSummaryDTO(
                entity.getId(),
                entity.getService().getServiceName(),
                entity.getClient().getNickname(),
                entity.getDeadline()
        );
    }
}
