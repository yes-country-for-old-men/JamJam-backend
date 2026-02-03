package com.jamjam.chat.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
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
    private String senderName;
    private String content;
    private LocalDateTime sentAt;

    @Column(nullable = false, columnDefinition = "varchar(255) default 'TEXT'")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "chat_message_files", joinColumns = @JoinColumn(name = "message_id"))
    @Builder.Default
    private List<ChatFileInfo> files = new ArrayList<>();

    public static ChatMessageEntity of(ChatRoomEntity room,
                                       String senderId,
                                       String content,
                                       String senderName,
                                       LocalDateTime sentAt) {
        return ChatMessageEntity.builder()
                .room(room)
                .senderId(senderId)
                .senderName(senderName)
                .content(content)
                .sentAt(sentAt)
                .build();
    }
}