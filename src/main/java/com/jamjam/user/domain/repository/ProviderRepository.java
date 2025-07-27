package com.jamjam.user.domain.repository;

import com.jamjam.user.domain.entity.ProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<ProviderEntity, Long> {
    Optional<ProviderEntity> findByUserId(Long userId);
} 