package com.jamjam.notify.domain.repository;

import com.jamjam.notify.domain.entity.FcmTokenEntity;
import com.jamjam.notify.domain.entity.UserNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {

    Optional<UserNotificationSetting> findByFcmToken(FcmTokenEntity fcmToken);

    Optional<UserNotificationSetting> findByFcmToken_Token(String token);
}
