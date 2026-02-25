package com.jamjam.chat.util;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.SocketEventType;
import com.jamjam.chat.domain.repository.ChatRoomParticipantRepository;
import com.jamjam.chat.presentation.dto.SocketEvent;
import com.jamjam.chat.presentation.dto.res.ChatRoomListRes;
import com.jamjam.chat.application.ChatService;
import com.jamjam.chat.presentation.dto.res.ChatSocketRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventBroadcaster {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomParticipantRepository partRepo;
    private final ChatService chatService;

    public void broadcastNewMessage(ChatMessageEntity savedMsg, String senderId) {
        Long roomId = savedMsg.getRoom().getId();
        ChatSocketRes dto = ChatSocketRes.from(savedMsg, savedMsg.getSenderName());

        partRepo.findByRoomId(roomId).forEach(part ->
                messagingTemplate.convertAndSendToUser(
                        part.getUserId(),
                        "/queue/chat",
                        new SocketEvent<>(SocketEventType.NEW_MESSAGE, dto)
                )
        );

        broadcastChatRoomUpdate(roomId);
        log.info("NEW_MESSAGE → room {} participants", roomId);
    }

    public void broadcastChatRoomUpdate(Long roomId) {
        partRepo.findByRoomId(roomId).forEach(part -> {
            String uid = part.getUserId();
            ChatRoomListRes.ChatRoomSummary summary = chatService.getChatRoomSummary(roomId, uid);
            messagingTemplate.convertAndSendToUser(
                    uid,
                    "/queue/room-updates",
                    new SocketEvent<>(SocketEventType.CHAT_ROOM_UPDATE, summary)
            );
            log.info("CHAT_ROOM_UPDATE → user {}", uid);
        });
    }

    public void broadcastMessageRead(Long roomId, Long lastReadMessageId) {
        partRepo.findByRoomId(roomId).forEach(part ->
                messagingTemplate.convertAndSendToUser(
                        part.getUserId(),
                        "/queue/chat",
                        new SocketEvent<>(SocketEventType.MESSAGE_READ, lastReadMessageId)
                )
        );
        log.info("MESSAGE_READ → room {} participants", roomId);
    }

    public void sendMessageToUser(String userId, Object message) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/messages",
                new SocketEvent<>(SocketEventType.SEND_MESSAGE, message)
        );
        log.info("sendMessageToUser → userId {}", userId);
    }
}