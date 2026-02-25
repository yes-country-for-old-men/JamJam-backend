package com.jamjam.chat.interceptor;

import com.jamjam.infra.jwt.application.JwtUtil;
import com.jamjam.user.application.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public Message<?> preSend(@NotNull Message<?> msg, @NotNull MessageChannel ch) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(msg, StompHeaderAccessor.class);
        if (acc == null) {
            return msg;
        }

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            String auth = acc.getFirstNativeHeader("Authorization");

            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new IllegalArgumentException("No Authorization header");
            }

            String token = auth.substring(7);

            if (!jwtUtil.validateToken(token)) {
                throw new IllegalArgumentException("Token invalid/expired");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            String userIdStr = String.valueOf(userId);
            UserDetails user = customUserDetailsService.loadUserByUserId(userId);

            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    userIdStr, null, user.getAuthorities());

            acc.setUser(authToken);
            acc.getSessionAttributes().put("userId", userIdStr);

            log.info("WebSocket 인증 완료: userId = {}", userIdStr);
        }
        return msg;
    }
}