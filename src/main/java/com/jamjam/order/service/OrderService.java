package com.jamjam.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.chat.application.ChatService;
import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.ChatRoomEntity;
import com.jamjam.chat.domain.entity.MessageType;
import com.jamjam.chat.domain.repository.ChatRoomRepository;
import com.jamjam.chat.presentation.dto.req.PaymentReq;
import com.jamjam.chat.util.EventBroadcaster;
import com.jamjam.global.exception.ApiException;
import com.jamjam.notify.domain.entity.NotificationType;
import com.jamjam.order.domain.entity.OrderEntity;
import com.jamjam.order.domain.entity.OrderReferenceFileEntity;
import com.jamjam.order.domain.entity.OrderStatus;
import com.jamjam.order.domain.repository.OrderReferenceFileRepository;
import com.jamjam.order.domain.repository.OrderRepository;
import com.jamjam.order.dto.*;
import com.jamjam.order.exception.OrderError;
import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.service.domain.repository.ServiceRepository;
import com.jamjam.service.util.S3Uploader;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.domain.entity.CreditChangeType;
import com.jamjam.user.domain.entity.CreditHistoryEntity;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.entity.UserRole;
import com.jamjam.user.domain.repository.CreditHistoryRepository;
import com.jamjam.user.domain.repository.UserRepository;
import com.jamjam.util.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;
    private final ServiceRepository serviceRepository;
    private final OrderReferenceFileRepository orderReferenceFileRepository;
    private final NotificationSender notificationSender;
    private final CreditHistoryRepository creditHistoryRepository;
    private final ChatService chatService;
    private final EventBroadcaster eventBroadcaster;
    private final ObjectMapper objectMapper;

    /*주문 신청*/
    @Transactional
    public void registerService(OrderRegisterRequest request, Long userId, List<MultipartFile> referenceFiles) {
        UserEntity user = userRepository.findByIdOrThrow(userId, OrderError.USER_NOT_FOUND);
        ServiceEntity service = serviceRepository.findByIdOrThrow(request.getServiceId(), OrderError.SERVICE_NOT_FOUND);
        /*본인 서비스에 신청 시*/
        if (service.getUser().getId().equals(user.getId())) throw new ApiException(OrderError.SELF_ORDER_NOT_ALLOWED);

        OrderEntity order = OrderEntity.builder()
                .title(request.getTitle())
                .deadline(request.getDeadline())
                .description(request.getDescription())
                .orderStatus(OrderStatus.REQUESTED)
                .price(BigDecimal.ZERO)
                .client(user)
                .service(service)
                .build();
        orderRepository.save(order);

        /*주문 참고 자료 이미지 저장*/
        try {
            if (referenceFiles != null) {
                for (MultipartFile file : referenceFiles) {
                    if (!file.isEmpty()) {
                        String fileUrl = s3Uploader.upload(file, "order-request-images");
                        OrderReferenceFileEntity fileInfo = new OrderReferenceFileEntity(fileUrl, order);

                        orderReferenceFileRepository.save(fileInfo);
                    }
                }
                log.info("[ORDER] 참고 자료 이미지 저장 완료");
            }
        } catch (IOException e) {
            throw new ApiException(OrderError.FILE_UPLOAD_ERROR);
        }
        log.info("[ORDER] 의뢰서 임시 저장 완료");

        // 서비스 제공자에게 의뢰서 송신
        String content = getContent(service, order);

        sendMessage(userId, order.getServiceProviderId(), MessageType.REQUEST_FORM, content);
    }
    /*주문 결제 요청*/
    @Transactional
    public void requestPayment(Long userId, PaymentReq request) {
        OrderEntity order = verifyProvider(userId, request.orderId(), OrderError.FORBIDDEN_REQUEST_PAYMENT);

        // 주문 상태가 REQUESTED일 때만 결제 요청 가능
        if (order.getOrderStatus() != OrderStatus.REQUESTED) {
            throw new ApiException(OrderError.CANNOT_REQUEST_PAYMENT);
        }
        // 기존 주문 금액과 조율 후 금액이 다른 경우 갱신
        BigDecimal newPrice = BigDecimal.valueOf(request.price());

        if (!order.getPrice().equals(newPrice)) order.setPrice(newPrice);

        // 주문자에게 결제 요청 송신
        String content;
        try {
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("orderId", order.getId());
            contentMap.put("price", newPrice);

            content = objectMapper.writeValueAsString(contentMap);
        } catch (JsonProcessingException e) {
            log.error("[ORDER] 메시지 포맷 변환 실패", e);
            throw new ApiException(OrderError.JSON_PROCESSING_ERROR);
        }

        sendMessage(userId, order.getClient().getId(), MessageType.REQUEST_PAYMENT, content);
    }
    /*결제 진행*/
    @Transactional
    public void processPayment(Long userId, PaymentReq request) {
        UserEntity client = userRepository.findByIdOrThrow(userId, OrderError.USER_NOT_FOUND);
        OrderEntity order = verifyClient(userId, request.orderId(), OrderError.FORBIDDEN_PROCESS_PAYMENT);

        if (order.getOrderStatus() != OrderStatus.REQUESTED) {
            throw new ApiException(OrderError.CANNOT_PROCESS_PAYMENT);
        }

        // 주문자 크레딧 차감 (히스토리 저장)
        BigDecimal amount = BigDecimal.valueOf(request.price());

        client.reduceCredit(amount, OrderError.CREDIT_NOT_ENOUGH);
        log.info("[ORDER] userID = {} Credit -{}", userId, amount);
        saveCreditHistory(amount, CreditChangeType.WITHDRAW,
                "서비스 의뢰로 인한 크레딧 차감", client);

        // 주문 상태 변경
        order.setOrderStatus(OrderStatus.PREPARING);

        // 제공자에게 결제 완료 알림
        String content = getContent(order.getService(), order);
        sendMessage(client.getId(), order.getServiceProviderId(), MessageType.PAYMENT_COMPLETED, content);
    }
    /*제공자의 주문 상태 변경*/
    @Transactional
    public void changeStatusOrder(Long providerId, OrderStatusRequest request) {
        OrderEntity order = verifyProvider(providerId, request.getOrderId(), OrderError.FORBIDDEN_CHANGE_ORDER_STATUS);

        order.changeStatus(request);
        orderRepository.save(order);
        log.info("주문 상태 변경 완료");

        String body;
        MessageType type;
        if (request.getOrderStatus() == OrderStatus.CANCELLED) {
            refundCreditOnCancellation(order.getClient(), order.getPrice());
            body = "\"" + order.getService().getServiceName() + "\" 서비스에 대한 주문이 취소되었습니다.";
            type = MessageType.ORDER_CANCELLED;
        }  else if (request.getOrderStatus() == OrderStatus.WAITING_CONFIRM) {
            body = "\"" + order.getService().getServiceName() + "\" 서비스에 대한 주문이 작업 완료되었습니다.";
            type = MessageType.WORK_COMPLETED;
        } else {
            throw new ApiException(OrderError.CANNOT_COMPLETE_ORDER);
        }
        String content = getContent(order.getService(), order);

        // 주문 상태 변경 주문자에게 알림
        sendMessage(providerId, order.getClient().getId(), type, content);

        notificationSender.sendToUser(
                order.getClient(),
                "주문 진행 상황",
                body,
                NotificationType.ORDER
        );
    }
    /*구매자의 주문 취소*/
    @Transactional
    public void cancelPurchase(Long userId, OrderStatusRequest request) {
        OrderEntity order = verifyClient(userId, request.getOrderId(), OrderError.FORBIDDEN_CHANGE_ORDER_STATUS);

        if (order.getOrderStatus() != OrderStatus.REQUESTED) {
            throw new ApiException(OrderError.CANNOT_CANCEL_AT_THIS_STATUS);
        }

        order.changeStatus(request);
        orderRepository.save(order);
        log.info("주문 취소 완료");
        refundCreditOnCancellation(order.getClient(), order.getPrice());

        // 주문 취소 제공자에게 알림
        String content = getContent(order.getService(), order);
        sendMessage(userId, order.getServiceProviderId(), MessageType.ORDER_CANCELLED, content);

        String body = "\"" + order.getService().getServiceName() + "\" 서비스에 대한 주문이 의뢰인에 의해 취소되었습니다.";
        notificationSender.sendToUser(
                order.getService().getUser(),
                "의뢰인이 주문 취소",
                body,
                NotificationType.ORDER
        );
    }
    /*구매자의 구매 확정*/
    @Transactional
    public void confirmPurchase(Long userId, Long orderId) {
        OrderEntity order = verifyClient(userId, orderId, OrderError.FORBIDDEN_CHANGE_ORDER_STATUS);

        order.forceComplete();
        orderRepository.save(order);
        log.info("주문 구매 확정 처리");

        transferCreditOnConfirmation(order.getServiceProviderId(), order.getPrice());

        notificationSender.sendToUser(
                order.getService().getUser(),
                "의뢰인이 구매 확정",
                "의뢰인이 구매 확정 하였습니다.",
                NotificationType.ORDER
        );
    }
    /*구매자 크레딧 제공자에게 전달*/
    @Transactional
    public void transferCreditOnConfirmation(Long providerId, BigDecimal price) {
        UserEntity provider = userRepository.findByIdOrThrow(providerId, OrderError.USER_NOT_FOUND);

        provider.addCredit(price);
        log.info("provider: {} credit: +{}", provider.getNickname(), price);
        userRepository.save(provider);
        log.info("크레딧 정산 완료");

        saveCreditHistory(price, CreditChangeType.DEPOSIT,
                "서비스 판매로 인한 입금", provider);
    }
    /*주문 취소 시, 크레딧 반환*/
    @Transactional
    public void refundCreditOnCancellation(UserEntity client, BigDecimal price) {
        client.addCredit(price);

        userRepository.save(client);
        log.info("주문 취소로 인한 {} 크레딧 반환 완료", price);

        saveCreditHistory(price, CreditChangeType.DEPOSIT,
                "주문 취소로 인한 크레딧 반환", client);
    }
    /*유저의 주문 상태 별 주문 목록 반환*/
    @Transactional
    public OrderListResponse getOrders(CustomUserDetails customUserDetails, OrderStatus orderStatus, Pageable pageable) {
        UserEntity user = userRepository.findByIdOrThrow(customUserDetails.getUserId(), OrderError.USER_NOT_FOUND);

        Page<OrderEntity> entities;
        if (user.getRole() == UserRole.PROVIDER) {
            entities = orderRepository.findByProviderIdAndOrderStatus(user.getId(), orderStatus, pageable);
        } else if (user.getRole() == UserRole.CLIENT) {
            entities = orderRepository.findByClientIdAndOrderStatus(user.getId(), orderStatus, pageable);
        } else {
            throw new ApiException(OrderError.UNKNOWN_USER_ROLE);
        }

        List<OrderSummaryDTO> dtoList = entities.stream()
                .map(OrderSummaryDTO::from)
                .toList();

        return OrderListResponse.builder()
                .orders(dtoList)
                .currentPage(entities.getNumber())
                .totalPages(entities.getTotalPages())
                .hasNext(entities.hasNext())
                .build();
    }
    /*유저의 상태 별 주문 갯수 반환*/
    @Transactional
    public OrderCountResponse getOrderCount(CustomUserDetails customUserDetails) {
        UserEntity user = userRepository.findByIdOrThrow(customUserDetails.getUserId(), OrderError.USER_NOT_FOUND);

        int preparing = 0, requested = 0, completed = 0, cancelled = 0;
        List<Object[]> result;

        if (user.getRole() == UserRole.PROVIDER) {
            result = orderRepository.countByStatusForProvider(user.getId());
        } else if (user.getRole() == UserRole.CLIENT) {
            result = orderRepository.countByStatusForClient(user.getId());
        } else {
            throw new ApiException(OrderError.UNKNOWN_USER_ROLE);
        }
        for (Object[] row : result) {
            OrderStatus status = (OrderStatus) row[0];
            Long count = (Long) row[1];
            switch (status) {
                case REQUESTED -> requested += count;
                case PREPARING -> preparing += count;
                case WAITING_CONFIRM, COMPLETED -> completed += count;
                case CANCELLED -> cancelled += count;
            }
        }
        return OrderCountResponse.builder()
                .requested(requested)
                .preparing(preparing)
                .completed(completed)
                .cancelled(cancelled)
                .build();
    }
    /*크레딧 사용 내역 저장*/
    @Transactional
    public void saveCreditHistory(BigDecimal price, CreditChangeType type, String reason, UserEntity user) {
        CreditHistoryEntity creditHistory = CreditHistoryEntity.builder()
                .amount(price)
                .type(type)
                .reason(reason)
                .user(user)
                .build();

        creditHistoryRepository.save(creditHistory);
        log.info("크레딧 내역 저장 완료");
    }
    /*주문에 관련 제공자인지 권한 확인*/
    public OrderEntity verifyProvider(Long userId, Long orderId, OrderError error) {
        OrderEntity order = orderRepository.findByIdOrThrow(orderId, OrderError.ORDER_NOT_FOUND);

        Long orderProviderId = order.getServiceProviderId();
        /*수락하는 user의 권한 확인*/
        if (!userId.equals(orderProviderId)) throw new ApiException(error);

        return order;
    }
    /*주문에 관련 구매자인지 권한 확인*/
    public OrderEntity verifyClient(Long userId, Long orderId, OrderError error) {
        OrderEntity order = orderRepository.findByIdOrThrow(orderId, OrderError.ORDER_NOT_FOUND);

        Long orderClientId = order.getClient().getId();

        if (!userId.equals(orderClientId)) throw new ApiException(error);

        return order;
    }
    /*주문 상세 정보 반환*/
    public OrderInfoDTO getOrderDetail(Long orderId) {
        OrderEntity order = orderRepository.findByIdOrThrow(orderId, OrderError.ORDER_NOT_FOUND);

        return OrderInfoDTO.from(order);
    }
    public String getContent(ServiceEntity service, OrderEntity order) {
        String content;
        try {
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("serviceId", service.getId());
            contentMap.put("serviceName", service.getServiceName());
            contentMap.put("serviceThumbnail", service.getThumbnail());
            contentMap.put("orderId", order != null ? order.getId() : null);

            content = objectMapper.writeValueAsString(contentMap);
        } catch (JsonProcessingException e) {
            log.error("[ORDER] 메시지 포맷 변환 실패", e);
            throw new ApiException(OrderError.JSON_PROCESSING_ERROR);
        }

        return content;
    }
    public void sendMessage(Long senderId, Long receiverId,  MessageType type, String content) {
        Long chatRoomId = chatService.getChatRoomId(senderId, receiverId);

        ChatMessageEntity savedMsg = chatService
                .sendMessage(chatRoomId, String.valueOf(senderId), content, type, null);
        eventBroadcaster.broadcastNewMessage(savedMsg, String.valueOf(senderId));
    }
}

