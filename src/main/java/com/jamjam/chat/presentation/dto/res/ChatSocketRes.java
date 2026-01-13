package com.jamjam.chat.presentation.dto.res;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.MessageType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record ChatSocketRes(
        Long messageId,
        String senderId,
        String senderNickname,
        String content,
        LocalDateTime sentAt,
        MessageType messageType,
        List<FileInfo> files
) {
    public static ChatSocketRes from(ChatMessageEntity entity, String senderNickname) {
        List<FileInfo> files = entity.getFiles().stream()
                .map(f -> new FileInfo(f.getFileUrl(), f.getFileName(), f.getFileSize(), f.getFileType()))
                .collect(Collectors.toList());

        return new ChatSocketRes(
                entity.getId(),
                entity.getSenderId(),
                senderNickname,
                entity.getContent(),
                entity.getSentAt(),
                entity.getMessageType(),
                files
        );
    }

    public record FileInfo(
            String fileUrl,
            String fileName,
            Long fileSize,
            MessageType fileType
    ) {}
}