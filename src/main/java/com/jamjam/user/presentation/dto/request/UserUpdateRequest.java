package com.jamjam.user.presentation.dto.request;

import com.jamjam.user.domain.entity.Gender;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserUpdateRequest(
        String name,
        String nickname,
        String phoneNumber,
        LocalDate birth,
        Gender gender,
        String profileUrl
) {
}
