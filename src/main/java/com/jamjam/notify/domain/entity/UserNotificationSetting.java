package com.jamjam.notify.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_notification_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fcm_token_id", nullable = false, unique = true)
    private FcmTokenEntity fcmToken;

    @Column(nullable = false)
    private boolean orderNotification = true;
    @Column(nullable = false)
    private boolean chatNotification = true;
    @Column(nullable = false)
    private boolean eventNotification = true;
}
