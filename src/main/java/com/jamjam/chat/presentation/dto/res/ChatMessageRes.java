package com.jamjam.chat.presentation.dto.res;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.MessageType;

import java.time.LocalDateTime;

public record ChatMessageRes(
        Long messageId,
        String senderId,
        String senderNickname,
        String content,
        LocalDateTime sentAt,
        Boolean isOwn,
        MessageType messageType,
        String fileUrl,
        String fileName,
        Long fileSize
) {
    public static ChatMessageRes from(ChatMessageEntity entity, String currentUserId, String senderNickname) {
        return new ChatMessageRes(
                entity.getId(),
                entity.getSenderId(),
                senderNickname,
                entity.getContent(),
                entity.getSentAt(),
                entity.getSenderId().equals(currentUserId),
                entity.getMessageType(),
                entity.getFileUrl(),
                entity.getFileName(),
                entity.getFileSize()
        );
    }
}