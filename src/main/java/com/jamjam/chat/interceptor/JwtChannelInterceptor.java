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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        log.info("🔍 JwtChannelInterceptor 실행됨");

        StompHeaderAccessor acc = StompHeaderAccessor.wrap(msg);
        log.info("📩 STOMP Command: {}", acc.getCommand());

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            log.info("🔗 WebSocket CONNECT 처리 시작");

            String auth = acc.getFirstNativeHeader("Authorization");
            if (auth == null) auth = (String) acc.getHeader("Authorization");

            log.info("🔑 Authorization 헤더: {}", auth != null ? "존재" : "없음");

            if (auth == null || !auth.startsWith("Bearer ")) {
                log.error("❌ Authorization 헤더가 없거나 잘못됨");
                throw new IllegalArgumentException("No Authorization header");
            }

            String token = auth.substring(7);
            log.info("🎫 토큰 추출 완료 (길이: {})", token.length());

            if (!jwtUtil.validateToken(token)) {
                log.error("❌ 토큰 유효성 검증 실패");
                throw new IllegalArgumentException("Token invalid/expired");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            String userIdStr = String.valueOf(userId);
            log.info("👤 토큰에서 추출한 userId: {}", userIdStr);

            UserDetails user = customUserDetailsService.loadUserByUserId(userId);
            log.info("👤 UserDetails 로드 완료 - username: {}", user.getUsername());

            // 🎯 핵심: Principal name을 userId로 설정
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    userIdStr,  // Principal name = userId
                    null,
                    user.getAuthorities());

            acc.setUser(authToken);
            SecurityContextHolder.getContext().setAuthentication(authToken);

            acc.getSessionAttributes().put("userId", userIdStr);

            log.info("✅ WebSocket 인증 완료");
            log.info("   - Principal name: {}", authToken.getName());
            log.info("   - Session userId: {}", userIdStr);
            log.info("   - 일치 여부: {}", userIdStr.equals(authToken.getName()));

        } else if (StompCommand.DISCONNECT.equals(acc.getCommand())) {
            log.info("🔌 WebSocket DISCONNECT");
        } else if (StompCommand.SEND.equals(acc.getCommand())) {
            log.debug("📤 STOMP SEND: {}", acc.getDestination());
        } else if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
            log.info("📥 STOMP SUBSCRIBE: {}", acc.getDestination());
        } else if (StompCommand.UNSUBSCRIBE.equals(acc.getCommand())) {
            log.info("📤 STOMP UNSUBSCRIBE: {}", acc.getDestination());
        }

        return msg;
    }
}