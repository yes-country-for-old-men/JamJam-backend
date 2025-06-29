package com.jamjam.chat.domain.repository;

import com.jamjam.chat.domain.entity.ChatRoomReadStatusEntity;
import com.jamjam.chat.domain.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatRoomReadStatusRepository extends JpaRepository<ChatRoomReadStatusEntity, Long> {
    Optional<ChatRoomReadStatusEntity> findByChatRoomAndUserId(ChatRoomEntity chatRoom, String userId);
} 