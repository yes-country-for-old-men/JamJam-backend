package com.jamjam.order.scheduler;

import com.jamjam.order.domain.entity.OrderEntity;
import com.jamjam.order.domain.repository.OrderRepository;
import com.jamjam.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderStatusScheduler {
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(cron = "0 0 * * * *")
    public void autoCompleteOrders() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<OrderEntity> orders = orderRepository.findWaitingConfirmOrdersBefore(threeDaysAgo);

        for (OrderEntity order : orders) {
            order.forceComplete();
        }
        orderRepository.saveAll(orders);
        log.info("자동 완료된 주문 수: {}", orders.size());

        for (OrderEntity order : orders) {
            Long clientId = order.getClient().getId();
            Long providerId = order.getService().getUser().getId();
            BigDecimal price = order.getPrice();
            orderService.transferCreditOnConfirmation(clientId, providerId, price);
        }
    }
}
