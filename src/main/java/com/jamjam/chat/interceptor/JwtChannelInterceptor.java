package com.jamjam.chat.interceptor;

import com.jamjam.infra.jwt.application.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> msg, MessageChannel ch) {

        StompHeaderAccessor acc = StompHeaderAccessor.wrap(msg);

        if (StompCommand.CONNECT.equals(acc.getCommand())) {

            String auth = acc.getFirstNativeHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer "))
                throw new IllegalArgumentException("No Authorization header");

            String token = auth.substring(7);
            if (!jwtUtil.validateToken(token))
                throw new IllegalArgumentException("Token invalid/expired");

            acc.getSessionAttributes()
                    .put("userId", jwtUtil.getUserIdFromToken(token));
        }
        return msg;
    }
}