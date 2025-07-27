package com.jamjam.notify.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record NotificationsResponse(
        List<NotificationSummary> notifications,
        Integer currentPage,
        Integer totalPages,
        Boolean hasNext
) {
}
