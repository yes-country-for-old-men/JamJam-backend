package com.jamjam.notify.dto;

import com.jamjam.notify.domain.entity.NotificationEntity;
import com.jamjam.notify.domain.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationSummary(
        Long id,
        String title,
        String content,
        boolean isRead,
        LocalDateTime createdAt,
        NotificationType type
) {
    public static NotificationSummary from(NotificationEntity entity) {
       return new NotificationSummary(entity.getId(),
               entity.getTitle(),
               entity.getContent(),
               entity.isRead(),
               entity.getCreatedAt(),
               entity.getType()
       );
    }
}
