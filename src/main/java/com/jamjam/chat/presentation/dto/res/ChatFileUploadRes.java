package com.jamjam.chat.presentation.dto.res;

import com.jamjam.chat.domain.entity.MessageType;

public record ChatFileUploadRes(
        String fileUrl,
        String fileName,
        Long fileSize,
        MessageType messageType
) {
}
