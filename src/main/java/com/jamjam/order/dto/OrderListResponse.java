package com.jamjam.order.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderListResponse(
        List<OrderSummaryDTO> orders,
        Integer currentPage,
        Integer totalPages,
        Boolean hasNext
) {
}
