package com.jamjam.user.presentation.dto.request;

public record SmsVerifyRequest(
        String phoneNumber,
        String code
) {
}
