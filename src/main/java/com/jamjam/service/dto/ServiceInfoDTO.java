package com.jamjam.service.dto;


import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.service.domain.entity.ServiceInfoImageEntity;
import com.jamjam.user.domain.entity.CareerEntity;

import java.util.*;
import java.util.stream.Collectors;

public record ServiceInfoDTO(
        Long serviceId,
        String thumbnail,
        Map<Long, String> portfolioImages,
        String serviceName,
        String description,
        Integer salary,
        Integer category
) {
    public static ServiceInfoDTO from(ServiceEntity entity) {
        List<ServiceInfoImageEntity> images = Optional.ofNullable(entity.getPortfolioImages())
                .orElse(Collections.emptyList());

        Map<Long, String> imageMap = images.stream()
                .collect(Collectors.toMap(
                        ServiceInfoImageEntity::getId,
                        ServiceInfoImageEntity::getImageUrl,
                        (v1, v2) -> v2 // 중복 발생 시 마지막 값으로 덮어쓰기
                ));
        return new ServiceInfoDTO(
                entity.getId(),
                entity.getThumbnail(),
                imageMap,
                entity.getServiceName(),
                entity.getDescription(),
                entity.getSalary(),
                entity.getCategoryId()
        );
    }
}
