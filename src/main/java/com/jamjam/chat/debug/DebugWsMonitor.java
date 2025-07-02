package com.jamjam.chat.debug;

import com.jamjam.chat.domain.entity.SocketEventType;
import com.jamjam.chat.presentation.dto.SocketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebugWsMonitor {

    private final SimpUserRegistry registry;
    private final SimpMessagingTemplate messagingTemplate;

    /** 10초마다 레지스트리 상태 출력 */
    @Scheduled(fixedRate = 10_000)
    public void logSessions() {
        registry.getUsers().forEach(u -> {
            String userName = u.getName();
            // Set<SimpSession>
            Set<SimpSession> sessions = u.getSessions();

            // 세션 ID만 뽑아서 보기 좋게 목록으로 변환
            List<String> ids = sessions.stream()
                    .map(SimpSession::getId)
                    .toList();

            log.info("[Registry] user={} sessions={}", userName, ids);
        });
    }

    /** 30초마다 userId=2 한 명에게 ping */
    @Scheduled(fixedRate = 5_000, initialDelay = 5_000)
    public void sendPing() {
        messagingTemplate.convertAndSendToUser(
                "2",
                "/queue/rooms",
                new SocketEvent<>(SocketEventType.CHAT_ROOM_UPDATE, "ping"));
        log.info("[Ping] sent to user 2");
    }
}