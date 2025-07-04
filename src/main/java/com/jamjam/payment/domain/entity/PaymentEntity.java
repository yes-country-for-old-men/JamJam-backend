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

    private BigDecimal price;
    private PaymentStatus status;
    private String paymentId;
    private LocalDateTime paidAt;

    @Builder
    public PaymentEntity(BigDecimal price, PaymentStatus status,
                         String paymentId, LocalDateTime paidAt) {
        this.price = price;
        this.status = status;
        this.paymentId = paymentId;
        this.paidAt = paidAt;
    }

    public void changePaymentBySuccess(PaymentStatus status, String paymentId) {
        this.status = status;
        this.paymentId = paymentId;
        this.paidAt = LocalDateTime.now();
    }
}
