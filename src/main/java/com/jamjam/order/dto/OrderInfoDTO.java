package com.jamjam.order.dto;

import com.jamjam.order.domain.entity.OrderEntity;

import java.time.LocalDate;
import java.util.List;

public record OrderInfoDTO(
        String title,
        LocalDate deadline,
        String description,
        String additionalRequest,
        List<String> orderImages
) {
    public static OrderInfoDTO from(OrderEntity entity) {
        return new OrderInfoDTO(
                entity.getTitle(),
                entity.getDeadline(),
                entity.getDescription(),
                entity.getAdditionalRequest(),
                entity.getOrderImages()
        );
    }
}
