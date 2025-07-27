package com.jamjam.order.domain.repository;

import com.jamjam.order.domain.entity.OrderEntity;
import com.jamjam.order.domain.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("SELECT o FROM OrderEntity o WHERE o.orderStatus = 3 AND o.serviceCompletedAt <= :purchaseDeadline")
    List<OrderEntity> findWaitingConfirmOrdersBefore(@Param("purchaseDeadline")LocalDateTime purchaseDeadline);

    @Query("SELECT o FROM OrderEntity o WHERE o.service.user.id = :providerId AND o.orderStatus = :orderStatus")
    Page<OrderEntity> findByProviderIdAndOrderStatus(Long providerId, OrderStatus orderStatus, Pageable pageable);

    @Query("SELECT o FROM OrderEntity o WHERE o.client.id = :clientId AND o.orderStatus = :orderStatus")
    Page<OrderEntity> findByClientIdAndOrderStatus(Long clientId, OrderStatus orderStatus, Pageable pageable);

    @Query("SELECT o.orderStatus, COUNT(o) FROM OrderEntity o " + "WHERE o.service.user.id = :providerId GROUP BY o.orderStatus")
    List<Object[]> countByStatusForProvider(@Param("providerId") Long providerId);

    @Query("SELECT o.orderStatus, COUNT(o) FROM OrderEntity  o " + "WHERE o.client.id = :clientId GROUP BY o.orderStatus")
    List<Object[]> countByStatusForClient(@Param("clientId") Long clientId);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.deadline >= :start AND o.deadline < :end " +
            "AND o.orderStatus = :status")
    List<OrderEntity> findByDeadlineBetweenAndStatus(LocalDateTime start, LocalDateTime end, OrderStatus status);
}
