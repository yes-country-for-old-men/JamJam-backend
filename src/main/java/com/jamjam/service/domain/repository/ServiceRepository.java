package com.jamjam.service.domain.repository;

import com.jamjam.service.domain.entity.ServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    @Query("SELECT s FROM ServiceEntity s LEFT JOIN s.user u WHERE s.category = :category")
    Page<ServiceEntity> findByCategory(Integer category, Pageable pageable);

    @Query("SELECT s FROM ServiceEntity s Left JOIN s.user u WHERE u.nickname = :nickname")
    Page<ServiceEntity> findByUserNickname(String nickname, Pageable pageable);

    Page<ServiceEntity> findAll(Pageable pageable);
}
