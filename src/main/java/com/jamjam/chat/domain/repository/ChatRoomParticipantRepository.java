package com.jamjam.chat.domain.repository;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.ChatRoomEntity;
import com.jamjam.chat.domain.entity.ChatRoomParticipantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomParticipantRepository
        extends JpaRepository<ChatRoomParticipantEntity, Long> {

    List<ChatRoomParticipantEntity> findByRoomId(Long roomId);

    boolean existsByRoomIdAndUserId(Long roomId, String userId);

    Page<ChatRoomParticipantEntity> findByUserId(String userId, Pageable pageable);

    Optional<ChatRoomParticipantEntity> findByRoomIdAndUserId(Long roomId, String userId);
}
