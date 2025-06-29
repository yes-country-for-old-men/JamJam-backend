package com.jamjam.service.dto;


import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.user.domain.entity.CareerEntity;

import java.util.List;

public record ServiceInfoDTO(
        String thumbnail,
        List<String> infoImages,
        String serviceName,
        String description,
        Integer salary,
        Integer category,
        List<CareerEntity> careers
) {
    public static ServiceInfoDTO from(ServiceEntity entity) {
        return new ServiceInfoDTO(
                entity.getThumbnail(),
                entity.getInfoImages(),
                entity.getServiceName(),
                entity.getDescription(),
                entity.getSalary(),
                entity.getCategoryId(),
                entity.getUser().getCareers()
        );
    }
}
