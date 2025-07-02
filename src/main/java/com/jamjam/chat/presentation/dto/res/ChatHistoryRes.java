package com.jamjam.chat.presentation.dto.res;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.global.dto.SliceInfo;

import java.util.List;
import java.util.Map;

public record ChatHistoryRes(
        SliceInfo sliceInfo,
        List<ChatMessageRes> chats
) {
    public static ChatHistoryRes of(List<ChatMessageEntity> entities, SliceInfo sliceInfo, String currentUserId, Map<String, String> userIdToNickname) {
        List<ChatMessageRes> dtos = entities.stream()
                .map(e -> ChatMessageRes.from(e, currentUserId, userIdToNickname.get(e.getSenderId())))
                .toList();
        return new ChatHistoryRes(sliceInfo, dtos);
    }
}
