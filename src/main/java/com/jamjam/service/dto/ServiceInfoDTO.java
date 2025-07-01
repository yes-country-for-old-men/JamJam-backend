package com.jamjam.service.dto;


import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.user.domain.entity.CareerEntity;

import java.util.List;
import java.util.UUID;

public record ServiceInfoDTO(
        UUID serviceId,
        String thumbnail,
        List<String> portfolioImages,
        String serviceName,
        String description,
        Integer salary,
        Integer category,
        List<CareerEntity> careers
) {
    public static ServiceInfoDTO from(ServiceEntity entity) {
        return new ServiceInfoDTO(
                entity.getId(),
                entity.getThumbnail(),
                entity.getPortfolioImages(),
                entity.getServiceName(),
                entity.getDescription(),
                entity.getSalary(),
                entity.getCategoryId(),
                entity.getUser().getCareers()
        );
    }
}
