package com.jamjam.notify.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.jamjam.global.exception.ApiException;
import com.jamjam.notify.domain.entity.FcmTokenEntity;
import com.jamjam.notify.domain.entity.NotificationEntity;
import com.jamjam.notify.domain.entity.NotificationType;
import com.jamjam.notify.domain.repository.FcmTokenRepository;
import com.jamjam.notify.domain.repository.NotificationRepository;
import com.jamjam.notify.dto.FcmTokenRequest;
import com.jamjam.notify.exception.NotifyError;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class FcmService {
    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public FcmService(FcmTokenRepository fcmTokenRepository, UserRepository userRepository, NotificationRepository notificationRepository) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    /*Fcm 토큰 저장*/
    public void addFcmToken(CustomUserDetails customUserDetails, FcmTokenRequest request) {
        Optional<FcmTokenEntity> existing = fcmTokenRepository.findByToken(request.getToken());
        if (existing.isEmpty()) {
            UserEntity user = userRepository.findById(customUserDetails.getUserId())
                    .orElseThrow(() -> new ApiException(NotifyError.USER_NOT_FOUND));
            log.info("user {}의 신규 토큰 저장", user.getNickname());

            FcmTokenEntity token = new FcmTokenEntity(user, request.getDevice(), request.getToken());
            fcmTokenRepository.save(token);
            log.info("토큰 저장 완료");
        }
    }
    /*Fcm 서버로 메시지 송신 요청*/
    public void sendMessage(UserEntity receiver, String targetToken, String title, String body, NotificationType type) {
        try {
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("푸시 알림 전송 완료: {}", response);

            NotificationEntity notification = NotificationEntity.builder()
                    .receiver(receiver)
                    .title(title)
                    .content(body)
                    .type(type)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
            log.info("알림 내역 저장 완료");
        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송 실패: {}", e.getMessage());
            throw new ApiException(NotifyError.CANNOT_SEND_MESSAGE);
        }

    }
}
