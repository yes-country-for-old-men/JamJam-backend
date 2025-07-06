package com.jamjam.payment.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jamjam.payment.domain.entity.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
}
