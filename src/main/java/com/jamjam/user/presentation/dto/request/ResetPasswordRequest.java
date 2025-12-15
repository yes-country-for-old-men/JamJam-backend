package com.jamjam.user.presentation.dto.request;

import java.time.LocalDate;

public record ResetPasswordRequest(
        String loginId,
        String name,
        LocalDate birth,
        String phoneNumber
) {
}