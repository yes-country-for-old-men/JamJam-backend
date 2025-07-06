package com.jamjam.user.domain.repository;

import com.jamjam.user.domain.entity.CreditHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditHistoryRepository extends JpaRepository<CreditHistoryEntity, Long> {
}
