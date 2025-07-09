package com.jamjam.service.domain.repository;

import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.service.dto.ServiceSummaryDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    @Query("SELECT s FROM ServiceEntity s LEFT JOIN s.user u WHERE s.categoryId = :category")
    Page<ServiceEntity> findByCategoryId(Integer category, Pageable pageable);

    @Query("SELECT s FROM ServiceEntity s Left JOIN s.user u WHERE u.id = :providerId")
    Page<ServiceEntity> findByUserId(Long providerId, Pageable pageable);

    Page<ServiceEntity> findAll(Pageable pageable);

    @Query("SELECT s FROM ServiceEntity s WHERE s.serviceName LIKE %:keyword% OR s.description LIKE %:keyword%")
    Page<ServiceEntity> findByKeyword(String keyword, Pageable pageable);

    @Query("SELECT s FROM ServiceEntity s Left JOIN s.user u WHERE u.nickname LIKE %:nickname%")
    Page<ServiceEntity> findByProvider(String nickname, Pageable pageable);
}
