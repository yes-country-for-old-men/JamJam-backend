package com.jamjam.order.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ClientOrderListResponse(
        List<ClientOrderSummaryDTO> orders,
        Integer currentPage,
        Integer totalPages,
        Boolean hasNext
) {
}
