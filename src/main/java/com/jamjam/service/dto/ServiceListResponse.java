package com.jamjam.service.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ServiceListResponse(
        List<ServiceSummaryDTO> services,
        Integer currentPage,
        Integer totalPages,
        Boolean hasNext
) {
}
