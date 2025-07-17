package com.jamjam.notify.service;

import com.jamjam.global.exception.ApiException;
import com.jamjam.notify.domain.entity.FcmTokenEntity;
import com.jamjam.notify.domain.entity.NotificationEntity;
import com.jamjam.notify.domain.entity.UserNotificationSetting;
import com.jamjam.notify.domain.repository.FcmTokenRepository;
import com.jamjam.notify.domain.repository.NotificationRepository;
import com.jamjam.notify.domain.repository.UserNotificationSettingRepository;
import com.jamjam.notify.dto.NotificationSettingRequest;
import com.jamjam.notify.dto.NotificationSettingResponse;
import com.jamjam.notify.dto.NotificationSummary;
import com.jamjam.notify.dto.NotificationsResponse;
import com.jamjam.notify.exception.NotifyError;
import com.jamjam.user.application.dto.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
public class NotifyService {
    private final UserNotificationSettingRepository settingRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationRepository notificationRepository;

    public NotifyService(UserNotificationSettingRepository settingRepository, FcmTokenRepository fcmTokenRepository, NotificationRepository notificationRepository) {
        this.settingRepository = settingRepository;
        this.fcmTokenRepository = fcmTokenRepository;
        this.notificationRepository = notificationRepository;
    }
    /*사용자 푸시 알림 설정*/
    @Transactional
    public void changeNotificationSetting(CustomUserDetails customUserDetails, NotificationSettingRequest request) {
        FcmTokenEntity fcmToken = fcmTokenRepository.findByUserIdAndDevice(customUserDetails.getUserId(), request.getDevice())
                .orElseThrow(() -> new ApiException(NotifyError.NOT_FOUND_FCM_TOKEN));
        UserNotificationSetting setting = settingRepository.findByFcmToken_Token(fcmToken.getToken())
                .orElseThrow(() -> new ApiException(NotifyError.INVALID_FCM_TOKEN));

        switch (request.getType()) {
            case ORDER -> setting.setOrderNotification(request.isAllowed());
            case CHAT -> setting.setChatNotification(request.isAllowed());
            case EVENT -> setting.setEventNotification(request.isAllowed());
        }
        settingRepository.save(setting);
    }
    /*사용자 푸시 알림 설정 상태 조회*/
    public NotificationSettingResponse getUserNotificationSetting(CustomUserDetails customUserDetails, String device) {
        FcmTokenEntity fcmToken = fcmTokenRepository.findByUserIdAndDevice(customUserDetails.getUserId(), device)
                .orElseThrow(() -> new ApiException(NotifyError.NOT_FOUND_FCM_TOKEN));
        UserNotificationSetting setting = settingRepository.findByFcmToken_Token(fcmToken.getToken())
                .orElseThrow(() -> new ApiException(NotifyError.INVALID_FCM_TOKEN));

        return NotificationSettingResponse.from(setting);
    }
    /*사용자 알림 내역 조회*/
    public NotificationsResponse getNotifications(Long userId, Pageable pageable) {
        Page<NotificationEntity> notifications = notificationRepository.findByReceiverId(userId, pageable);
        //TODO: 채팅 알림 내역 포함 상태임
        List<NotificationSummary> dtoList = notifications.stream()
                .map(NotificationSummary::from)
                .toList();

        return NotificationsResponse.builder()
                .notifications(dtoList)
                .currentPage(notifications.getNumber() + 1)
                .totalPages(notifications.getTotalPages())
                .hasNext(notifications.hasNext())
                .build();
    }
}
