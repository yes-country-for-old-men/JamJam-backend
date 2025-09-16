package com.jamjam.user.presentation.dto.response;

import com.jamjam.user.domain.entity.CreditHistoryEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditHistorySummary(
        Long id,
        String amount,
        String reason,
        LocalDateTime createdAt
) {
    public static CreditHistorySummary from(CreditHistoryEntity entity) {
        BigDecimal amt = entity.getAmount();
        String formatted = (amt.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + amt.toPlainString();
        return new CreditHistorySummary(
                entity.getId(),
                formatted,
                entity.getReason(),
                entity.getCreatedAt()
        );
    }
}
