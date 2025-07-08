package com.jamjam.order.domain.repository;

import com.jamjam.order.domain.entity.OrderEntity;
import com.jamjam.order.domain.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("SELECT o FROM OrderEntity o WHERE o.orderStatus = 3 AND o.serviceCompletedAt <= :purchaseDeadline")
    List<OrderEntity> findWaitingConfirmOrdersBefore(@Param("purchaseDeadline")LocalDateTime purchaseDeadline);

    @Query("SELECT o FROM OrderEntity o WHERE o.service.user.id = :providerId AND o.orderStatus = :orderStatus")
    List<OrderEntity> findByProviderIdAndOrderStatus(Long providerId, OrderStatus orderStatus);

    @Query("SELECT o FROM OrderEntity o WHERE o.client.id = :clientId AND o.orderStatus = :orderStatus")
    List<OrderEntity> findByClientIdAndOrderStatus(Long clientId, OrderStatus orderStatus);
}
