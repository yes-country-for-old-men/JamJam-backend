package com.jamjam.chat.domain.repository;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.ChatRoomEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessageEntity, Long> {

    Slice<ChatMessageEntity> findByRoomIdOrderBySentAtDesc(Long roomId, Pageable pageable);

    ChatMessageEntity findTopByRoomOrderBySentAtDesc(ChatRoomEntity room);

    int countByRoomIdAndIdGreaterThanAndSenderIdNot(Long id, Long lastReadMessageId, String userId);
}
