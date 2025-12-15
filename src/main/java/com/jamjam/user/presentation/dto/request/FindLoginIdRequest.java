package com.jamjam.user.presentation.dto.request;

import java.time.LocalDate;

public record FindLoginIdRequest(
        String name,
        LocalDate birth,
        String phoneNumber
) {
}