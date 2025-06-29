package com.jamjam.chat.util;

import com.jamjam.chat.domain.entity.ChatRoomParticipantEntity;
import com.jamjam.chat.domain.entity.SocketEventType;
import com.jamjam.chat.domain.repository.ChatRoomParticipantRepository;
import com.jamjam.chat.presentation.dto.SocketEvent;
import com.jamjam.chat.presentation.dto.res.ChatRoomListRes;
import com.jamjam.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventBroadcaster {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomParticipantRepository partRepo;
    private final ChatService chatService;

    public void broadcastNewMessage(Long roomId, String senderId, String content) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                new SocketEvent<>(SocketEventType.NEW_MESSAGE, content)
        );
        broadcastChatRoomUpdate(roomId);
    }

    public void broadcastChatRoomUpdate(Long roomId) {
        List<String> participantIds = partRepo.findByRoomId(roomId)
                .stream().map(ChatRoomParticipantEntity::getUserId).toList();
        for (String userId : participantIds) {
            ChatRoomListRes.ChatRoomSummary summary = chatService.getChatRoomSummary(roomId, userId);
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/rooms",
                    new SocketEvent<>(SocketEventType.CHAT_ROOM_UPDATE, summary)
            );
        }
    }

    public void broadcastMessageRead(Long roomId, Long lastReadMessageId) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                new SocketEvent<>(SocketEventType.MESSAGE_READ, lastReadMessageId)
        );
        broadcastChatRoomUpdate(roomId);
    }

    public void sendMessageToUser(String userId, Object message) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/messages",
                new SocketEvent<>(SocketEventType.SEND_MESSAGE, message)
        );
    }
}