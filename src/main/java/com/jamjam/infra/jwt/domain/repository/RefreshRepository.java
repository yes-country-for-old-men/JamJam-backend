package com.jamjam.infra.jwt.domain.repository;

import com.jamjam.infra.jwt.domain.entity.RefreshEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long>{

    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);

    void deleteByExpiresBefore(LocalDateTime now);
}
