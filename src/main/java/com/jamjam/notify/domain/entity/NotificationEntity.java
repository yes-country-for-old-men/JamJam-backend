package com.jamjam.notify.domain.entity;

import com.jamjam.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity receiver;

    private String title;
    private String content;

    private boolean isRead = false;

    private LocalDateTime createdAt;

    private NotificationType type;
}
