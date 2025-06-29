package com.jamjam.chat.domain.repository;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.ChatRoomEntity;
import com.jamjam.global.dto.SliceInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessageEntity, Long> {

    Slice<ChatMessageEntity> findByRoomIdOrderBySentAtDesc(Long roomId, Pageable pageable);

    ChatMessageEntity findTopByRoomOrderBySentAtDesc(ChatRoomEntity room);

    Integer countByRoomIdAndIdGreaterThan(Long roomId, Long lastReadMessageId);
}
