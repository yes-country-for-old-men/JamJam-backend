package com.jamjam.notify.dto;

import com.jamjam.notify.domain.entity.NotificationType;
import lombok.Getter;

@Getter
public class NotificationSettingRequest {
    private String device;
    private NotificationType type;
    private boolean allowed;
}
