package com.jamjam.order.domain.repository;

import com.jamjam.order.domain.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("SELECT o FROM OrderEntity o WHERE o.orderStatus = 3 AND o.serviceCompletedAt <= :purchaseDeadline")
    List<OrderEntity> findWaitingConfirmOrdersBefore(@Param("purchaseDeadline")LocalDateTime purchaseDeadline);
}
