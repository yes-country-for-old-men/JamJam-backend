package com.jamjam.chat.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomEntity {

    @Id @GeneratedValue
    private Long id;

    private boolean groupChat;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<ChatRoomParticipantEntity> participants = new ArrayList<>();
}