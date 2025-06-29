package com.jamjam.chat.presentation.dto.req;

public record MessageReadReq(
    Long roomId,
    Long lastReadMessageId
) {} 