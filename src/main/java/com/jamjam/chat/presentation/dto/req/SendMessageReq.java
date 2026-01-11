package com.jamjam.chat.presentation.dto.req;

import com.jamjam.chat.domain.entity.MessageType;

public record SendMessageReq(
        Long roomId,
        String message,
        MessageType messageType,
        String fileUrl,
        String fileName,
        Long fileSize
) {
}
