package com.jamjam.service.dto;


import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.service.domain.entity.ServiceInfoImageEntity;
import com.jamjam.user.domain.entity.CareerEntity;

import java.util.*;
import java.util.stream.Collectors;

public record ServiceInfoDTO(
        Long userId,
        Long serviceId,
        String thumbnail,
        List<PortfolioImageDTO> portfolioImages,
        String serviceName,
        String description,
        Integer salary,
        Integer category
) {
    public static ServiceInfoDTO from(ServiceEntity entity) {
        List<PortfolioImageDTO> imageList = Optional.ofNullable(entity.getPortfolioImages())
                .orElse(Collections.emptyList())
                .stream()
                .map(img -> new PortfolioImageDTO(img.getId(), img.getImageUrl()))
                .toList();

        return new ServiceInfoDTO(
                entity.getUser().getId(),
                entity.getId(),
                entity.getThumbnail(),
                imageList,
                entity.getServiceName(),
                entity.getDescription(),
                entity.getSalary(),
                entity.getCategoryId()
        );
    }
    public record PortfolioImageDTO(Long id, String url) {}
}
