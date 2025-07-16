package com.jamjam.notify.dto;

import com.jamjam.notify.domain.entity.UserNotificationSetting;
import lombok.Builder;

public record NotificationSettingResponse (
        boolean event,
        boolean order,
        boolean chat
) {
    public static NotificationSettingResponse from(UserNotificationSetting setting) {
        return new NotificationSettingResponse(
                setting.isEventNotification(),
                setting.isOrderNotification(),
                setting.isChatNotification()
        );
    }
}
