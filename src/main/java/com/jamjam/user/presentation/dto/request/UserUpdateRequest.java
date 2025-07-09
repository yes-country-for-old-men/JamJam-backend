package com.jamjam.user.presentation.dto.request;

import com.jamjam.user.domain.entity.Gender;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserUpdateRequest(
        String nickname,
        String phoneNumber,
        String password,
        String accountNumber,
        String depositor,
        Boolean deleteProfileImage
) {
}
