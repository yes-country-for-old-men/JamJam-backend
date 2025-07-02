package com.jamjam.chat.util;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.ChatRoomParticipantEntity;
import com.jamjam.chat.domain.entity.SocketEventType;
import com.jamjam.chat.domain.repository.ChatMessageRepository;
import com.jamjam.chat.domain.repository.ChatRoomParticipantRepository;
import com.jamjam.chat.presentation.dto.SocketEvent;
import com.jamjam.chat.presentation.dto.res.ChatMessageRes;
import com.jamjam.chat.presentation.dto.res.ChatRoomListRes;
import com.jamjam.chat.application.ChatService;
import com.jamjam.chat.presentation.dto.res.ChatSocketRes;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventBroadcaster {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomParticipantRepository partRepo;
    private final ChatService chatService;
    private final UserRepository userRepo;

    public void broadcastNewMessage(ChatMessageEntity savedMsg, String senderId) {
        Long roomId = savedMsg.getRoom().getId();

        UserEntity sender   = userRepo.findById(Long.valueOf(senderId)).orElse(null);
        String senderNickname   = sender != null ? sender.getNickname() : null;

        ChatSocketRes dto  = new ChatSocketRes(
                savedMsg.getId(),
                savedMsg.getSenderId(),
                senderNickname,
                savedMsg.getContent(),
                savedMsg.getSentAt()
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                new SocketEvent<>(SocketEventType.NEW_MESSAGE, dto)
        );
        broadcastChatRoomUpdate(roomId);
        log.info("broadcast → /topic/room/{}", roomId);
    }

    public void broadcastChatRoomUpdate(Long roomId) {

        partRepo.findByRoomId(roomId).forEach(part -> {
            String uid = String.valueOf(part.getUserId());

            ChatRoomListRes.ChatRoomSummary summary =
                    chatService.getChatRoomSummary(roomId, uid);

            try{
                messagingTemplate.convertAndSendToUser(
                        uid,
                        "/queue/rooms",
                        new SocketEvent<>(SocketEventType.CHAT_ROOM_UPDATE, summary));
                log.info("CHAT_ROOM_UPDATE → participant {}", uid);
            } catch (Exception e){
                log.error("CHAT_ROOM_UPDATE ERROR ==> {}", e.getMessage());
            }
        });

        log.info("CHAT_ROOM_UPDATE → participants of room {}", roomId);
    }

    public void broadcastMessageRead(Long roomId, Long lastReadMessageId) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                new SocketEvent<>(SocketEventType.MESSAGE_READ, lastReadMessageId)
        );
        broadcastChatRoomUpdate(roomId);
        log.info("MESSAGE_READ → /topic/room/{}", roomId);
    }

    public void sendMessageToUser(String userId, Object message) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/messages",
                new SocketEvent<>(SocketEventType.SEND_MESSAGE, message)
        );
    }
}