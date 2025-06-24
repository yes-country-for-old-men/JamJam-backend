package com.jamjam.user.presentation.dto.response;

import com.jamjam.user.domain.entity.Gender;
import com.jamjam.user.domain.entity.UserRole;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserResponse(
        String name,
        String nickname,
        String phoneNumber,
        String loginId,
        LocalDate birth,
        UserRole role,
        Gender gender,
        String profileUrl
){
}
