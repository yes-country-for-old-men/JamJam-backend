package com.jamjam.service.dto;

import com.jamjam.service.domain.entity.ServiceEntity;

public record ServiceSummaryDTO(
        String thumbnailUrl,
        String serviceName,
        String providerName,
        int salary
) {
    public static ServiceSummaryDTO from(ServiceEntity entity) {
        String nickname = (entity.getUser() != null) ? entity.getUser().getNickname() : "알 수 없음";

        return new ServiceSummaryDTO(
                entity.getThumbnail(),
                entity.getServiceName(),
                entity.getUser().getNickname(),
                entity.getSalary()
        );
    }
}