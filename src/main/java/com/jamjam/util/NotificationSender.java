package com.jamjam.util;

import com.jamjam.notify.domain.entity.FcmTokenEntity;
import com.jamjam.notify.domain.entity.NotificationType;
import com.jamjam.notify.domain.entity.UserNotificationSetting;
import com.jamjam.notify.domain.repository.UserNotificationSettingRepository;
import com.jamjam.notify.service.FcmService;
import com.jamjam.user.domain.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationSender {
    private final FcmService fcmService;
    private final UserNotificationSettingRepository settingRepository;

    public NotificationSender(FcmService fcmService, UserNotificationSettingRepository settingRepository) {
        this.fcmService = fcmService;
        this.settingRepository = settingRepository;
    }

    public void sendToUser(UserEntity receiver, String title, String body, NotificationType type) {
        for (FcmTokenEntity token : receiver.getFcmTokens()) {
            UserNotificationSetting setting = settingRepository.findByFcmToken(token)
                    .orElse(null);

            if (!isNotificationEnabled(setting, type)) {
                return;
            }

            fcmService.sendMessage(receiver, token.getToken(), title, body, type);
        }
    }
    private boolean isNotificationEnabled(UserNotificationSetting setting, NotificationType type) {
        if (setting == null) return false;

        return switch (type) {
            case REQUEST, DEADLINE ->  setting.isOrderNotification();
            case CHAT -> setting.isChatNotification();
            case EVENT -> setting.isEventNotification();
            default -> false;
        };
    }
}
