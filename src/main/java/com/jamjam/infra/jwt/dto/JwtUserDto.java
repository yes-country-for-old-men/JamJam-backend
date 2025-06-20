package com.jamjam.infra.jwt.dto;

import lombok.Builder;

@Builder
public record JwtUserDto(
        String userEmail,
        String password,
        Long userId,
        String role
) {
}