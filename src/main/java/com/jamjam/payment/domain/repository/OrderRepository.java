package com.jamjam.payment.domain.repository;

import com.jamjam.payment.domain.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("SELECT o FROM OrderEntity o WHERE o.merchantUid = :merchantUid")
    Optional<OrderEntity> findByMerchantUid(String merchantUid);
}
