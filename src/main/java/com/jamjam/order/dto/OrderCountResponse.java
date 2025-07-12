package com.jamjam.order.dto;

import lombok.Builder;

@Builder
public record OrderCountResponse (
        Integer preparing,
        Integer completed,
        Integer cancelled
){ }
