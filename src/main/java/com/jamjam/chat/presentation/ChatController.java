    package com.jamjam.chat.presentation;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.jamjam.chat.presentation.dto.SocketEvent;
    import com.jamjam.chat.presentation.dto.req.MarkAsReadReq;
    import com.jamjam.chat.presentation.dto.req.MessageReadReq;
    import com.jamjam.chat.presentation.dto.req.SendMessageReq;
    import com.jamjam.chat.presentation.dto.res.ChatHistoryRes;
    import com.jamjam.chat.presentation.dto.res.ChatRoomListRes;
    import com.jamjam.chat.presentation.dto.res.CreateRoomRes;
    import com.jamjam.chat.service.ChatService;
    import com.jamjam.global.annotation.CurrentUser;
    import com.jamjam.global.dto.ResponseDto;
    import com.jamjam.global.dto.SuccessMessage;
    import com.jamjam.user.application.dto.CustomUserDetails;
    import com.jamjam.chat.util.EventBroadcaster;
    import lombok.RequiredArgsConstructor;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.web.PageableDefault;
    import org.springframework.http.ResponseEntity;
    import org.springframework.messaging.handler.annotation.DestinationVariable;
    import org.springframework.messaging.handler.annotation.MessageMapping;
    import org.springframework.messaging.handler.annotation.Payload;
    import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
    import org.springframework.messaging.simp.SimpMessagingTemplate;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.DeleteMapping;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.PutMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.PathVariable;
    
    import java.util.ArrayList;
    import java.util.List;

    import static com.jamjam.chat.domain.entity.SocketEventType.SEND_MESSAGE;

    @RequiredArgsConstructor
    @Controller
    @RequestMapping("/api/chat")
    public class ChatController {
    
        private final ChatService chatService;
        private final SimpMessagingTemplate messagingTemplate;
        private final EventBroadcaster eventBroadcaster;
        private ObjectMapper objectMapper;

        @MessageMapping("/chat")
        public void handleChatEvent(@Payload SocketEvent<?> event, SimpMessageHeaderAccessor accessor) {
            String userId = accessor.getSessionAttributes().get("userId").toString();

            switch (event.type()) {
                case SEND_MESSAGE -> {
                    SendMessageReq req = convert(event.content(), SendMessageReq.class);
                    chatService.sendMessage(req.roomId(), userId, req.message());
                    eventBroadcaster.broadcastNewMessage(req.roomId(), userId, req.message());
                }
                case MESSAGE_READ -> {
                    MessageReadReq req = convert(event.content(), MessageReadReq.class);
                    chatService.markRoomAsRead(req.roomId(), userId, req.lastReadMessageId());
                    eventBroadcaster.broadcastMessageRead(req.roomId(), req.lastReadMessageId());
                }
            }
        }
    
        @PostMapping("/room")
        public ResponseEntity<ResponseDto<CreateRoomRes>> makeRoom(
                @CurrentUser CustomUserDetails user,
                @RequestParam Long serviceId
        ){
            String userIdStr = chatService.getUserIdFromService(serviceId);
            List<String> userIds = new ArrayList<>();
            userIds.add(userIdStr);
            userIds.add(String.valueOf(user.getUserId()));
    
            return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                    chatService.createRoom(false, userIds)));
        }

        @GetMapping("/rooms/{chatRoomId}/messages")
        public ResponseEntity<ResponseDto<ChatHistoryRes>> getMessages(
                @PathVariable Long chatRoomId,
                @PageableDefault Pageable pageable
        ) {
            ChatHistoryRes slice = chatService.getHistory(chatRoomId, pageable);
            return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, slice));
        }

        @PutMapping("/rooms/{chatRoomId}/read")
        public ResponseEntity<?> markRoomAsRead(
                @CurrentUser CustomUserDetails user,
                @PathVariable Long chatRoomId,
                @RequestBody MarkAsReadReq request
        ) {
            chatService.markRoomAsRead(chatRoomId, String.valueOf(user.getUserId()), request.lastReadMessageId());
            eventBroadcaster.broadcastMessageRead(chatRoomId, request.lastReadMessageId());
            return ResponseEntity.ok().build();
        }

        @GetMapping("/rooms")
        public ResponseEntity<ResponseDto<ChatRoomListRes>> getChatRooms(
                @CurrentUser CustomUserDetails user,
                @PageableDefault Pageable pageable
        ) {
            ChatRoomListRes res = chatService.getChatRooms(String.valueOf(user.getUserId()), pageable);
            return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, res));
        }

        @DeleteMapping("/rooms/{chatRoomId}")
        public ResponseEntity<?> leaveRoom(
                @CurrentUser CustomUserDetails user,
                @PathVariable Long chatRoomId
        ) {
            chatService.leaveRoom(chatRoomId, String.valueOf(user.getUserId()));
            return ResponseEntity.ok().build();
        }

        private <T> T convert(Object content, Class<T> clazz) {
            return objectMapper.convertValue(content, clazz);
        }
    }
