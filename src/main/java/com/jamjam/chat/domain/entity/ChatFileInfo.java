package com.jamjam.chat.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatFileInfo {
    private String fileUrl;
    private String fileName;
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    private MessageType fileType;
}