package com.jamjam.user.domain.repository;

import com.jamjam.user.domain.entity.SmsVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SmsVerificationRepository extends JpaRepository<SmsVerificationEntity, Long>{

    @Modifying
    @Transactional
    @Query("DELETE FROM SmsVerificationEntity s WHERE s.expiresAt < :now")
    void deleteByExpiresAtBefore(@Param("now") LocalDateTime now);

    Optional<SmsVerificationEntity> findByPhoneNumber(String phoneNumber);

    void deleteByPhoneNumber(String phoneNumber);
}
