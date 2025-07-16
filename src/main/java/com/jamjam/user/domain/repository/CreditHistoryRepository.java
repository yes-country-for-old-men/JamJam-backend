package com.jamjam.user.domain.repository;

import com.jamjam.user.domain.entity.CreditChangeType;
import com.jamjam.user.domain.entity.CreditHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditHistoryRepository extends JpaRepository<CreditHistoryEntity, Long> {

    Page<CreditHistoryEntity> findByUserIdAndType(Long userId, CreditChangeType type, Pageable pageable);

    Page<CreditHistoryEntity> findByUserId(Long userId, Pageable pageable);
}
