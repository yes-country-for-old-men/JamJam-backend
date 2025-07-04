package com.jamjam.payment.domain.entity;

import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@Table(name="orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal price;
    private String merchantUid;

    /*소비자*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ServiceEntity service;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime orderedAt;

    @Builder
    public OrderEntity(BigDecimal price, String merchantUid,
                       UserEntity user, ServiceEntity service,
                       PaymentEntity payment) {
        this.price = price;
        this.merchantUid = merchantUid;
        this.user = user;
        this.service = service;
        this.payment = payment;
    }
}
