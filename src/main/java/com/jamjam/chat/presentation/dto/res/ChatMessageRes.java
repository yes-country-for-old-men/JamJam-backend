package com.jamjam.chat.presentation.dto.res;

import com.jamjam.chat.domain.entity.ChatFileInfo;
import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.MessageType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record ChatMessageRes(
        Long messageId,
        String senderId,
        String senderNickname,
        String content,
        LocalDateTime sentAt,
        Boolean isOwn,
        MessageType messageType,
        List<FileInfo> files
) {
    public static ChatMessageRes from(ChatMessageEntity entity, String currentUserId, String senderNickname) {
        List<FileInfo> files = entity.getFiles().stream()
                .map(f -> new FileInfo(f.getFileUrl(), f.getFileName(), f.getFileSize(), f.getFileType()))
                .collect(Collectors.toList());

        return new ChatMessageRes(
                entity.getId(),
                entity.getSenderId(),
                senderNickname,
                entity.getContent(),
                entity.getSentAt(),
                entity.getSenderId().equals(currentUserId),
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