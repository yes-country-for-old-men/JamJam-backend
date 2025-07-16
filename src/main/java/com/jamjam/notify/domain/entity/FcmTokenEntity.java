package com.jamjam.notify.domain.entity;

import com.jamjam.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "fcmTokens")
public class FcmTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(length = 512, nullable = false)
    private String token;

    public FcmTokenEntity(UserEntity user, String device, String token) {
        this.user = user;
        this.device = device;
        this.token = token;
    }
}
