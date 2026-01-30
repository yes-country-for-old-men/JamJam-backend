package com.jamjam.chat.domain.repository;

import com.jamjam.chat.domain.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    @Query("SELECT r FROM ChatRoomEntity r " +
           "WHERE r.groupChat = false " +
           "AND SIZE(r.participants) = 2 " +
           "AND EXISTS (SELECT 1 FROM ChatRoomParticipantEntity p WHERE p.room = r AND p.userId = :userId1) " +
           "AND EXISTS (SELECT 1 FROM ChatRoomParticipantEntity p WHERE p.room = r AND p.userId = :userId2)")
    Optional<ChatRoomEntity> findExistingDirectChat(@Param("userId1") String userId1, @Param("userId2") String userId2);
  
  @Query("SELECT r FROM ChatRoomEntity r " +
            "JOIN r.participants p1 " +
            "JOIN r.participants p2 " +
            "WHERE p1.userId = :userId1 " +
            "AND p2.userId = :userId2 " +
            "AND r.groupChat = false")
    Optional<ChatRoomEntity> findPrivateChatRoom(@Param("userId1") Long userId1,
                                                 @Param("userId2") Long userID2);
}
