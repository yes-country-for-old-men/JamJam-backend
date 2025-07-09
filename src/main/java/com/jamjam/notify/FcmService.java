package com.jamjam.notify;

import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.exception.ApiException;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class FcmService {
    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    public FcmService(FcmTokenRepository fcmTokenRepository, UserRepository userRepository) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.userRepository = userRepository;
    }

    /*Fcm 토큰 저장*/
    public void addFcmToken(CustomUserDetails customUserDetails, FcmTokenRequest request) {
        Optional<FcmTokenEntity> existing = fcmTokenRepository.findByToken(request.getToken());
        if (existing.isEmpty()) {
            UserEntity user = userRepository.findById(customUserDetails.getUserId())
                    .orElseThrow(() -> new ApiException(NotifyError.USER_NOT_FOUND));
            log.info("user {}의 신규 토큰 저장", user.getNickname());

            FcmTokenEntity token = new FcmTokenEntity(user, request.getToken());
            fcmTokenRepository.save(token);
            log.info("토큰 저장 완료");
        }
    }
}
