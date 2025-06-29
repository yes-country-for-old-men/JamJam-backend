package com.jamjam.chat.presentation.dto.res;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.global.dto.SliceInfo;

import java.util.List;

public record ChatHistoryRes(
        SliceInfo sliceInfo,
        List<ChatMessageEntity> chats
) {
    public static ChatHistoryRes of(List<ChatMessageEntity> coupons, SliceInfo sliceInfo) {
        return new ChatHistoryRes(sliceInfo, coupons);
    }
}
