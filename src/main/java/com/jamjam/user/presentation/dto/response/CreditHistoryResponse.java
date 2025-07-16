package com.jamjam.user.presentation.dto.response;

import com.jamjam.order.dto.OrderSummaryDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record CreditHistoryResponse(
        List<CreditHistorySummary> histories,
        Integer currentPage,
        Integer totalPages,
        Boolean hasNext
) {
}
