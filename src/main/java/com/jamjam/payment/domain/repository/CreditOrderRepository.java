package com.jamjam.payment.domain.repository;

import com.jamjam.payment.domain.entity.CreditOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CreditOrderRepository extends JpaRepository<CreditOrderEntity, Long> {

    @Query("SELECT o FROM CreditOrderEntity o WHERE o.payment.paymentUid = :paymentUid")
    Optional<CreditOrderEntity> findByPaymentPaymentUid(String paymentUid);
}
