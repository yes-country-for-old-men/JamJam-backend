package com.jamjam.infra.jwt.dto;

import lombok.Builder;

@Builder
public record JwtUserDto(
        Long   userId,
        String loginId,
        String password,
        String role
) {}