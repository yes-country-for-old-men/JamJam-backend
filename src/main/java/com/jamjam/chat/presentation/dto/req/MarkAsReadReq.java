package com.jamjam.chat.presentation.dto.req;

import lombok.Getter;
import lombok.Setter;

public record MarkAsReadReq(
        Long lastReadMessageId
) {
} 