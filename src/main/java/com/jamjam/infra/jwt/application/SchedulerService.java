package com.jamjam.infra.jwt.application;

import com.jamjam.infra.jwt.domain.repository.RefreshRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
public class SchedulerService {
    private final RefreshRepository refreshRepository;

    public SchedulerService(RefreshRepository refreshRepository) {
        this.refreshRepository = refreshRepository;
    }

    @Scheduled(fixedRate = 60 * 60 * 24 * 1000)
    public void deleteAllExpires() {
        LocalDateTime now = LocalDateTime.now();
        refreshRepository.deleteByExpiresBefore(now);
    }
}

