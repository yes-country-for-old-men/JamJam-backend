package com.jamjam.infra.jwt.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="refresh")
public class RefreshEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String refresh;
    @Column(name = "expires", columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private LocalDateTime expires;

    @Builder
    public RefreshEntity(Long userId, String refresh, LocalDateTime expires) {
        this.userId = userId;
        this.refresh = refresh;
        this.expires = expires;
    }
}
