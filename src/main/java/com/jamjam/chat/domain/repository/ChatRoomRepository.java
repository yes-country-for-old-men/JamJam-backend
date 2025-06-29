package com.jamjam.chat.domain.repository;

import com.jamjam.chat.domain.entity.ChatRoomEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {
    Page<ChatRoomEntity> findByUserIdAndNameContaining(String userId, String search, Pageable pageable);
}
