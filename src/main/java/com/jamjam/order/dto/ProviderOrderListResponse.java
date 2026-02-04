package com.jamjam.order.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProviderOrderListResponse(
        List<ProviderOrderSummaryDTO> orders,
        Integer currentPage,
        Integer totalPages,
        Boolean hasNext
) {
}
