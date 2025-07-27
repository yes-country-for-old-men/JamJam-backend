package com.jamjam.order.domain.repository;

import com.jamjam.order.domain.entity.OrderReferenceFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderReferenceFileRepository extends JpaRepository<OrderReferenceFileEntity, Long> {
}
