package com.jamjam.chat.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatMessageEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id")
    private ChatRoomEntity room;
    private String senderId;
    private String content;
    private LocalDateTime sentAt;

    public static ChatMessageEntity of(ChatRoomEntity room,
                                       String senderId,
                                       String content,
                                       LocalDateTime sentAt) {
        return ChatMessageEntity.builder()
                .room(room)
                .senderId(senderId)
                .content(content)
                .sentAt(sentAt)
                .build();
    }
}