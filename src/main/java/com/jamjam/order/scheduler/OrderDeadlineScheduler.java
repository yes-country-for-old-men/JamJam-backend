package com.jamjam.order.scheduler;


import com.jamjam.notify.domain.entity.NotificationType;
import com.jamjam.order.domain.entity.OrderEntity;
import com.jamjam.order.domain.entity.OrderStatus;
import com.jamjam.order.domain.repository.OrderRepository;
import com.jamjam.util.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderDeadlineScheduler {
    private final OrderRepository orderRepository;
    private final NotificationSender notificationSender;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void notifyProvidersAboutDeadline() {
        List<Integer> daysBeforeDeadline = List.of(7, 3, 1);

        for (Integer days : daysBeforeDeadline) {
            LocalDate targetDate = LocalDate.now().plusDays(days);
            LocalDateTime start = targetDate.atStartOfDay();
            LocalDateTime end = targetDate.plusDays(1).atStartOfDay();

            List<OrderEntity> orders = orderRepository.findByDeadlineBetweenAndStatus(start, end, OrderStatus.PREPARING);

            for (OrderEntity order : orders) {
                StringBuilder titleBuilder = new StringBuilder(20);
                titleBuilder.append("마감일 ")
                        .append(days)
                        .append("일 전");

                StringBuilder bodyBuilder = new StringBuilder(40);
                bodyBuilder.append("마감일까지 ")
                        .append(days)
                        .append("일 남은 주문 건이 있습니다.");

                notificationSender.sendToUser(
                        order.getService().getUser(),
                        titleBuilder.toString(),
                        bodyBuilder.toString(),
                        NotificationType.ORDER
                );
            }
        }

    }
}
