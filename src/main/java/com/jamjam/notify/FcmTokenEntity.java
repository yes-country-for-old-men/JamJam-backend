package com.jamjam.notify;

import com.jamjam.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "fcmTokens")
public class FcmTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(length = 512, nullable = false)
    private String token;

    public FcmTokenEntity(UserEntity user, String token) {
        this.user = user;
        this.token = token;
    }
}
