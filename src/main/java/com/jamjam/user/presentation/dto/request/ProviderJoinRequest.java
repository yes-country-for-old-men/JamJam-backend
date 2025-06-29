package com.jamjam.user.presentation.dto.request;

import com.jamjam.user.domain.entity.Gender;

import java.time.LocalDate;

public record ProviderJoinRequest(
        String name,
        String nickname,
        String loginId,
        String phoneNumber,
        String password,
        LocalDate birth,
        Gender gender
) {
}