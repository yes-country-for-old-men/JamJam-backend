package com.jamjam.chat.presentation.dto.res;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.MessageType;

import java.time.LocalDateTime;

public record ChatSocketRes(
        Long messageId,
        String senderId,
        String senderNickname,
        String content,
        LocalDateTime sentAt,
        MessageType messageType,
        String fileUrl,
        String fileName,
        Long fileSize
) {
    public static ChatSocketRes from(ChatMessageEntity entity, String senderNickname) {
        return new ChatSocketRes(
                entity.getId(),
                entity.getSenderId(),
                senderNickname,
                entity.getContent(),
                entity.getSentAt(),
                entity.getMessageType(),
                entity.getFileUrl(),
                entity.getFileName(),
                entity.getFileSize()
        );
    }
}