package com.jamjam.chat.service;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.ChatRoomEntity;
import com.jamjam.chat.domain.entity.ChatRoomParticipantEntity;
import com.jamjam.chat.domain.entity.ChatRoomReadStatusEntity;
import com.jamjam.chat.domain.repository.ChatMessageRepository;
import com.jamjam.chat.domain.repository.ChatRoomParticipantRepository;
import com.jamjam.chat.domain.repository.ChatRoomRepository;
import com.jamjam.chat.domain.repository.ChatRoomReadStatusRepository;
import com.jamjam.chat.presentation.dto.res.ChatHistoryRes;
import com.jamjam.chat.presentation.dto.res.ChatRoomListRes;
import com.jamjam.chat.presentation.dto.res.CreateRoomRes;
import com.jamjam.global.dto.SliceInfo;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository roomRepo;
    private final ChatRoomParticipantRepository partRepo;
    private final ChatMessageRepository msgRepo;
    private final ChatRoomReadStatusRepository readStatusRepo;
    private final UserRepository userRepo;
//    private final ServiceService serviceService;

    @Transactional
    public void sendMessage(Long roomId, String senderId, String content) {

        ChatRoomEntity room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        log.info("sending message to room {} from {} to {}", room, senderId, content);

        if (!partRepo.existsByRoomIdAndUserId(roomId, senderId))
            throw new SecurityException("당신은 이 방의 멤버가 아닙니다");

        msgRepo.save(
                ChatMessageEntity.builder()
                        .room(room)
                        .senderId(senderId)
                        .content(content)
                        .sentAt(LocalDateTime.now())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public ChatHistoryRes getHistory(Long roomId, Pageable pageable) {
        Slice<ChatMessageEntity> chatSlice = msgRepo.findByRoomIdOrderBySentAtDesc(roomId, pageable);

        List<ChatMessageEntity> chats = chatSlice.getContent();

        SliceInfo sliceInfo = SliceInfo.of(chatSlice.hasNext());
        return ChatHistoryRes.of(chats, sliceInfo);
    }

    @Transactional
    public CreateRoomRes createRoom(boolean groupChat, List<String> userIds) {

        ChatRoomEntity room = roomRepo.save(ChatRoomEntity.builder()
                .groupChat(groupChat)
                .createdAt(LocalDateTime.now())
                .build());

        userIds.forEach(uid ->
                partRepo.save(ChatRoomParticipantEntity.builder()
                        .room(room)
                        .userId(uid)
                        .build()));

        return new CreateRoomRes(room.getId());
    }

    @Transactional(readOnly = true)
    public ChatRoomListRes getChatRooms(String userId, Pageable pageable) {
        Page<ChatRoomParticipantEntity> page = partRepo.findByUserId(userId, pageable);

        List<ChatRoomListRes.ChatRoomSummary> roomSummaries = page.getContent().stream()
                .map(participant -> getChatRoomSummary(participant.getRoom().getId(), userId))
                .toList();

        return ChatRoomListRes.builder()
                .rooms(roomSummaries)
                .currentPage(pageable.getPageNumber() + 1)
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .build();
    }

    public String getUserIdFromService(Long serviceId) {
        // serviceService.getUserId(serviceId);
        return "3";
    }

    @Transactional
    public void markRoomAsRead(Long roomId, String userId, Long lastReadMessageId) {
        ChatRoomEntity room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        ChatRoomReadStatusEntity status = readStatusRepo.findByChatRoomAndUserId(room, userId)
                .orElse(ChatRoomReadStatusEntity.builder()
                        .chatRoom(room)
                        .userId(userId)
                        .lastReadMessageId(lastReadMessageId)
                        .updatedAt(LocalDateTime.now())
                        .build());
        status.updateLastRead(lastReadMessageId);
        readStatusRepo.save(status);
    }

    public ChatRoomListRes.ChatRoomSummary getChatRoomSummary(Long roomId, String userId) {
        ChatRoomEntity room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        String opponentId = room.getParticipants().stream()
                .map(ChatRoomParticipantEntity::getUserId)
                .filter(id -> !id.equals(userId))
                .findFirst()
                .orElse(null);

        UserEntity opponent = opponentId != null
                ? userRepo.findById(Long.valueOf(opponentId)).orElse(null)
                : null;

        ChatMessageEntity lastMessage = msgRepo.findTopByRoomOrderBySentAtDesc(room);

        ChatRoomReadStatusEntity readStatus = readStatusRepo.findByChatRoomAndUserId(room, userId)
                .orElse(null);

        Long lastReadMessageId = readStatus != null ? readStatus.getLastReadMessageId() : 0L;

        int unreadCount = msgRepo.countByRoomIdAndIdGreaterThan(room.getId(), lastReadMessageId);

        return ChatRoomListRes.ChatRoomSummary.builder()
                .id(room.getId())
                .nickname(opponent != null ? opponent.getNickname() : null)
                .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getSentAt() : null)
                .unreadCount(unreadCount)
                .profileUrl(opponent != null ? opponent.getProfileUrl() : null)
                .build();
    }

    @Transactional
    public void leaveRoom(Long roomId, String userId) {
        ChatRoomEntity room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // 참가자에서 삭제
        ChatRoomParticipantEntity participant = partRepo.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("참가자가 아닙니다"));
        partRepo.delete(participant);

        // 읽음 상태도 함께 삭제
        readStatusRepo.findByChatRoomAndUserId(room, userId)
                .ifPresent(readStatusRepo::delete);
    }
}