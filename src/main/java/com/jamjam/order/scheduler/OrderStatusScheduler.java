package com.jamjam.order.scheduler;

import com.jamjam.notify.domain.entity.NotificationType;
import com.jamjam.order.domain.entity.OrderEntity;
import com.jamjam.order.domain.repository.OrderRepository;
import com.jamjam.order.service.OrderService;
import com.jamjam.util.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderStatusScheduler {
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final NotificationSender notificationSender;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void autoCompleteOrders() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<OrderEntity> orders = orderRepository.findWaitingConfirmOrdersBefore(threeDaysAgo);

        for (OrderEntity order : orders) {
            order.forceConfirmed();
        }
        orderRepository.saveAll(orders);
        log.info("자동 완료된 주문 수: {}", orders.size());

        for (OrderEntity order : orders) {
            Long providerId = order.getService().getUser().getId();
            BigDecimal price = order.getPrice();
            orderService.transferCreditOnConfirmation(providerId, price);

            notificationSender.sendToUser(
                    order.getService().getUser(),
                    "주문이 자동 구매 확정",
                    "구매 확정 기간이 지나 자동 처리되었습니다.",
                    NotificationType.ORDER
            );
        }
    }
}
