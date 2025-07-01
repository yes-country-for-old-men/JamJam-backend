package com.jamjam.search.service;

import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.service.domain.repository.ServiceRepository;
import com.jamjam.service.dto.ServiceSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class SearchService {
    private final ServiceRepository serviceRepository;

    public SearchService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public Page<ServiceSummaryDTO> getServiceByKeyword(String keyword, String nickname, Pageable pageable) {
        Page<ServiceEntity> entities;
        if (keyword != null) {
            entities = serviceRepository.findByKeyword(keyword, pageable);
            log.info("키워드 기반 서비스 조회 성공");
        } else if (nickname != null) {
            entities = serviceRepository.findByProvider(nickname, pageable);
            log.info("제공자 닉네임 기반 서비스 조회 성공");
        } else {
            entities = serviceRepository.findAll(pageable);
        }

        return entities.map(ServiceSummaryDTO::from);
    }
}
