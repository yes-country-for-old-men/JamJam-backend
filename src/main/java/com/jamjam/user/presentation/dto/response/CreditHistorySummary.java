package com.jamjam.user.presentation.dto.response;

import com.jamjam.user.domain.entity.CreditHistoryEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditHistorySummary(
        BigDecimal amount,
        String reason,
        LocalDateTime createdAt
) {
    public static CreditHistorySummary from(CreditHistoryEntity entity) {
        return new CreditHistorySummary(
                entity.getAmount(),
                entity.getReason(),
                entity.getCreatedAt()
        );
    }
}
