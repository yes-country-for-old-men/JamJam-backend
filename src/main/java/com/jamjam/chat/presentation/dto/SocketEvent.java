package com.jamjam.chat.presentation.dto;

import com.jamjam.chat.domain.entity.SocketEventType;

public record SocketEvent<T> (
        SocketEventType type,
        T content
) {
}
