package com.jamjam.payment.domain.entity;

import com.jamjam.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@Table(name="credit_orders")
public class CreditOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal price;
    /*소비자*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime orderedAt;

    @Builder
    public CreditOrderEntity(BigDecimal price, UserEntity user,
                             PaymentEntity payment) {
        this.price = price;
        this.user = user;
        this.payment = payment;
    }
}
