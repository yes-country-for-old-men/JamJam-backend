package com.jamjam.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.chat.application.ChatService;
import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.MessageType;
import com.jamjam.chat.util.EventBroadcaster;
import com.jamjam.global.exception.ApiException;
import com.jamjam.order.domain.entity.OrderEntity;
import com.jamjam.order.exception.OrderError;
import com.jamjam.service.domain.entity.ServiceEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WSNotificationService {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final EventBroadcaster eventBroadcaster;

    public String getContent(ServiceEntity service, OrderEntity order) {
        String content;
        try {
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("serviceId", service.getId());
            contentMap.put("serviceName", service.getServiceName());
            contentMap.put("serviceThumbnail", service.getThumbnail());
            contentMap.put("orderId", order != null ? order.getId() : null);

            content = objectMapper.writeValueAsString(contentMap);
        } catch (JsonProcessingException e) {
            log.error("[ORDER] 메시지 포맷 변환 실패", e);
            throw new ApiException(OrderError.JSON_PROCESSING_ERROR);
        }

        return content;
    }
    public void sendMessage(Long senderId, Long receiverId, MessageType type, String content) {
        Long chatRoomId = chatService.getChatRoomId(senderId, receiverId);

        ChatMessageEntity savedMsg = chatService
                .sendMessage(chatRoomId, String.valueOf(senderId), content, type, null);
        eventBroadcaster.broadcastNewMessage(savedMsg, String.valueOf(senderId));
    }
}
