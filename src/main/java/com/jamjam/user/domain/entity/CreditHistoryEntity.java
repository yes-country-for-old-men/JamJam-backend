package com.jamjam.user.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "credit_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;  //변동 크레딧 갯수

    @Column(nullable = false)
    private CreditChangeType type;

    private String reason;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Builder
    public CreditHistoryEntity(BigDecimal amount, CreditChangeType type,
                               String reason, UserEntity user) {
        this.amount = amount;
        this.type = type;
        this.reason = reason;
        this.user = user;
    }
}
