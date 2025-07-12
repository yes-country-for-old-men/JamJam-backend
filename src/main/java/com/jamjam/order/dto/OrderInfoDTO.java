package com.jamjam.order.dto;

import com.jamjam.order.domain.entity.OrderEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record OrderInfoDTO(
        String title,
        Long clientId,
        Long providerId,
        LocalDateTime deadline,
        String description,
        List<ReferenceFileDTO> referenceFiles,
        String cancelReason
) {
    public static OrderInfoDTO from(OrderEntity entity) {
        List<ReferenceFileDTO> fileList = Optional.ofNullable(entity.getReferenceFiles())
                .orElse(Collections.emptyList())
                .stream()
                .map(file -> new ReferenceFileDTO(file.getId(), file.getFileUrl()))
                .toList();

        return new OrderInfoDTO(
                entity.getTitle(),
                entity.getClient().getId(),
                entity.getService().getUser().getId(),
                entity.getDeadline(),
                entity.getDescription(),
                fileList,
                entity.getCancelReason()
        );
    }
    public record ReferenceFileDTO(Long id, String url) {}
}
