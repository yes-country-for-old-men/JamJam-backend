package com.jamjam.user.presentation.dto.request;

import com.jamjam.user.domain.entity.AccountDto;
import lombok.Builder;


@Builder
public record UserUpdateRequest(
        String nickname,
        String phoneNumber,
        String password,
        AccountDto account,
        Boolean deleteProfileImage
) {
}
