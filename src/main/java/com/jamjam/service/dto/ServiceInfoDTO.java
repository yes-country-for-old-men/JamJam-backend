package com.jamjam.service.dto;


import com.jamjam.service.domain.entity.ServiceEntity;

import java.util.*;

public record ServiceInfoDTO(
        Long userId,
        String nickName,
        String profileUrl,
        String location,
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
                entity.getUser().getNickname(),
                entity.getUser().getProfileUrl(),
                entity.getUser().getProvider().getLocation(),
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
