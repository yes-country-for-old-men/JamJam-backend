package com.jamjam.payment.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@Table(name = "payments")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private PaymentStatus status;
    private String paymentUid;
    private LocalDateTime paidAt;

    @Builder
    public PaymentEntity(PaymentStatus status, String paymentUid,
                         LocalDateTime paidAt) {
        this.status = status;
        this.paymentUid = paymentUid;
        this.paidAt = paidAt;
    }

    public void changePaymentBySuccess(PaymentStatus status, String paymentUid) {
        this.status = status;
        this.paymentUid = paymentUid;
        this.paidAt = LocalDateTime.now();
    }
}
