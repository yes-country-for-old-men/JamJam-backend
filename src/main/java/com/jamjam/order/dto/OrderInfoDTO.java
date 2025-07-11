package com.jamjam.order.dto;

import com.jamjam.order.domain.entity.OrderEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderInfoDTO(
        String title,
        Long clientId,
        Long providerId,
        LocalDateTime deadline,
        String description,
        List<String> referenceFiles,
        String cancelReason
) {
    public static OrderInfoDTO from(OrderEntity entity) {
        return new OrderInfoDTO(
                entity.getTitle(),
                entity.getClient().getId(),
                entity.getService().getUser().getId(),
                entity.getDeadline(),
                entity.getDescription(),
                entity.getReferenceFiles(),
                entity.getCancelReason()
        );
    }
}
