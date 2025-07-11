package com.jamjam.service.domain.repository;

import com.jamjam.service.domain.entity.ServiceInfoImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceInfoImageRepository extends JpaRepository<ServiceInfoImageEntity, Long> {
}
