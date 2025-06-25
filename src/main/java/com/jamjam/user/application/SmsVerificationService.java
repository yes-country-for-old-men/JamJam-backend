package com.jamjam.user.application;

import com.jamjam.global.exception.ApiException;
import com.jamjam.infra.sms.provider.CoolSmsProvider;
import com.jamjam.user.domain.entity.SmsVerificationEntity;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.repository.SmsVerificationRepository;
import com.jamjam.user.domain.repository.UserRepository;
import com.jamjam.user.exception.UserError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Transactional
public class SmsVerificationService {

    private final CoolSmsProvider coolSmsProvider;
    private final SmsVerificationRepository smsVerificationRepository;
    private final UserRepository userRepository;

    public SmsVerificationService(
            SmsVerificationRepository smsVerificationRepository, 
            CoolSmsProvider coolSmsProvider,
            UserRepository userRepository) {
        this.smsVerificationRepository = smsVerificationRepository;
        this.coolSmsProvider = coolSmsProvider;
        this.userRepository = userRepository;
    }

    public void sendMessage(String phoneNumber) throws Exception {
        LocalDateTime now = LocalDateTime.now();

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new ApiException(UserError.LOGIN_INPUT_EMPTY);
        }

        String purePhoneNumber = removeHyphens(phoneNumber);

        smsVerificationRepository.deleteByPhoneNumber(purePhoneNumber);

        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        int randomNumber = 100_000 + secureRandom.nextInt(900_000);

        SmsVerificationEntity entity = SmsVerificationEntity.builder()
                .phoneNumber(purePhoneNumber)
                .createdAt(now)
                .expiresAt(now.plusMinutes(5))
                .verificationCode(String.valueOf(randomNumber))
                .build();

        smsVerificationRepository.save(entity);

        coolSmsProvider.sendVerificationCode(purePhoneNumber, String.valueOf(randomNumber));
    }

    public void verifyCode(String phoneNumber, String code) {
        String purePhoneNumber = removeHyphens(phoneNumber);

        SmsVerificationEntity verificationEntity = smsVerificationRepository.findByPhoneNumber(purePhoneNumber)
                .orElseThrow(() -> new ApiException(UserError.VERIFICATION_NOT_FOUND));

        if (isExpired(verificationEntity)) {
            throw new ApiException(UserError.VERIFICATION_IS_EXPIRED);
        }

        if (!isEqual(verificationEntity, code)) {
            throw new ApiException(UserError.VERIFICATION_NOT_MATCH);
        }

        smsVerificationRepository.delete(verificationEntity);
    }

    public String removeHyphens(String phoneNumber) {
        return phoneNumber.replace("-", "");
    }

    public boolean isExpired(SmsVerificationEntity verificationEntity) {
        LocalDateTime expiresAt = verificationEntity.getExpiresAt();
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isEqual(SmsVerificationEntity verificationEntity, String code) {
        return verificationEntity.getVerificationCode().equals(code);
    }
}
