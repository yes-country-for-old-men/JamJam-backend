package com.jamjam.chat.presentation.dto.req;

public record SendMessageReq(
        Long roomId,
        String message
) {
}
