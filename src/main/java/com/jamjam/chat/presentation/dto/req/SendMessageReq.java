package com.jamjam.chat.presentation.dto.req;

import com.jamjam.chat.domain.entity.MessageType;

import java.util.List;

public record SendMessageReq(
        Long roomId,
        String message,
        MessageType messageType,
        List<FileInfo> files
) {
    public record FileInfo(
            String fileUrl,
            String fileName,
            Long fileSize,
            MessageType fileType
    ) {}
}
