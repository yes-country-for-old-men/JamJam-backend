package com.jamjam.order.service;

import com.jamjam.global.exception.ApiException;
import com.jamjam.notify.domain.entity.FcmTokenEntity;
import com.jamjam.notify.domain.entity.NotificationType;
import com.jamjam.notify.service.FcmService;
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
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.entity.UserRole;
import com.jamjam.user.domain.repository.UserRepository;
import com.jamjam.util.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;
    private final ServiceRepository serviceRepository;
    private final OrderReferenceFileRepository orderReferenceFileRepository;
    private final NotificationSender notificationSender;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        S3Uploader s3Uploader, ServiceRepository serviceRepository,
                        OrderReferenceFileRepository orderReferenceFileRepository, NotificationSender notificationSender) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.s3Uploader = s3Uploader;
        this.serviceRepository = serviceRepository;
        this.orderReferenceFileRepository = orderReferenceFileRepository;
        this.notificationSender = notificationSender;
    }
    /*주문 신청*/
    @Transactional
    public void registerService(OrderRegisterRequest request, Long userId, List<MultipartFile> referenceFiles) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(OrderError.USER_NOT_FOUND));
        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ApiException(OrderError.SERVICE_NOT_FOUND));
        /*보유 크레딧과 주문 가격 비교*/
        if (user.getCredit().compareTo(request.getPrice()) < 0) throw new ApiException(OrderError.CREDIT_NOT_ENOUGH);

        user.changeCredit(request.getPrice().negate());
        log.info("client {} 크레딧 차감", request.getPrice());

        OrderEntity order = OrderEntity.builder()
                .title(request.getTitle())
                .deadline(request.getDeadline())
                .description(request.getDescription())
                .orderStatus(OrderStatus.REQUESTED)
                .price(request.getPrice())
                .client(user)
                .service(service)
                .build();
        orderRepository.save(order);

        /*주문 내용 저장*/
        try {
            if (referenceFiles != null) {
                for (MultipartFile file : referenceFiles) {
                    if (!file.isEmpty()) {
                        String fileUrl = s3Uploader.upload(file, "order-request-images");
                        OrderReferenceFileEntity fileInfo = OrderReferenceFileEntity.builder()
                                .fileUrl(fileUrl)
                                .order(order)
                                .build();
                        orderReferenceFileRepository.save(fileInfo);
                    }
                }
                log.info("참고 자료 이미지 저장 완료");
            }
        } catch (IOException e) {
            throw new ApiException(OrderError.FILE_UPLOAD_ERROR);
        }

        log.info("서비스 신청 완료");

        /*해당 서비스 제공자에게 푸시 알림*/
        String body = "\"" + service.getServiceName() + "\" 서비스에 새로운 주문이 요청되었습니다.";
        notificationSender.sendToUser(
                service.getUser(),
                "신규 주문 등록",
                body,
                NotificationType.ORDER);
    }
    /*제공자의 주문 상태 변경*/
    @Transactional
    public void changeStatusOrder(Long providerId, OrderStatusRequest request) {
        OrderEntity order = verifyProvider(providerId, request.getOrderId());

        order.changeStatus(request);
        orderRepository.save(order);
        log.info("주문 상태 변경 완료");

        String body;
        if (request.getOrderStatus() == OrderStatus.CANCELLED) {
            refundCreditOnCancellation(order.getClient(), order.getPrice());
            body = "\"" + order.getService().getServiceName() + "\" 서비스에 대한 주문이 취소되었습니다.";
        } else if (request.getOrderStatus() == OrderStatus.PREPARING) {
            body = "\"" + order.getService().getServiceName() + "\" 서비스에 대한 주문이 수락되었습니다.";
        } else if (request.getOrderStatus() == OrderStatus.WAITING_CONFIRM) {
            body = "\"" + order.getService().getServiceName() + "\" 서비스에 대한 주문이 작업 완료되었습니다.";
        } else {
            throw new ApiException(OrderError.CANNOT_COMPLETE_ORDER);
        }

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
        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ApiException(OrderError.ORDER_NOT_FOUND));

        if (!userId.equals(order.getClient().getId())) {
            throw new ApiException(OrderError.FORBIDDEN_CHANGE_ORDER_STATUS);
        }
        if (order.getOrderStatus() != OrderStatus.REQUESTED) {
            throw new ApiException(OrderError.CANNOT_CANCEL_AT_THIS_STATUS);
        }

        order.changeStatus(request);
        orderRepository.save(order);
        log.info("주문 취소 완료");
        refundCreditOnCancellation(order.getClient(), order.getPrice());

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
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(OrderError.ORDER_NOT_FOUND));
        if (!userId.equals(order.getClient().getId())) {
            throw new ApiException(OrderError.FORBIDDEN_CHANGE_ORDER_STATUS);
        }
        order.forceComplete();
        orderRepository.save(order);
        log.info("주문 구매 확정 처리");

        transferCreditOnConfirmation(order.getService().getUser().getId(), order.getPrice());

        notificationSender.sendToUser(
                order.getService().getUser(),
                "의뢰인이 구매 확정",
                "의뢰인이 구매 확정 하였습니다.",
                NotificationType.ORDER
        );
    }
    /*수락하는 user의 권한 확인 메서드*/
    public OrderEntity verifyProvider(Long userId, Long orderId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(OrderError.USER_NOT_FOUND));
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(OrderError.ORDER_NOT_FOUND));
        Long orderProviderId = order.getService().getUser().getId();
        /*수락하는 user의 권한 확인*/
        if (!user.getId().equals(orderProviderId)) {
            throw new ApiException(OrderError.FORBIDDEN_CHANGE_ORDER_STATUS);
        }
        return order;
    }
    /*구매자 크레딧 제공자에게 전달*/
    @Transactional
    public void transferCreditOnConfirmation(Long providerId, BigDecimal price) {
        UserEntity provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ApiException(OrderError.USER_NOT_FOUND));

        provider.changeCredit(price);
        log.info("provider: {} credit: +{}", provider.getNickname(), price);
        userRepository.save(provider);
        log.info("크레딧 정산 완료");
    }
    /*주문 취소 시, 크레딧 반환*/
    @Transactional
    public void refundCreditOnCancellation(UserEntity client, BigDecimal price) {
        client.changeCredit(price);

        userRepository.save(client);
        log.info("주문 취소로 인한 {} 크레딧 반환 완료", price);
    }
    /*제공자의 주문 상태 별 주문 목록 반환*/
    @Transactional
    public OrderListResponse getOrders(CustomUserDetails customUserDetails, OrderStatus orderStatus, Pageable pageable) {
        UserEntity user = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new ApiException(OrderError.USER_NOT_FOUND));

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
                .currentPage(entities.getNumber() + 1)
                .totalPages(entities.getTotalPages())
                .hasNext(entities.hasNext())
                .build();
    }
    /*유저의 상태 별 주문 갯수 반환*/
    @Transactional
    public OrderCountResponse getOrderCount(CustomUserDetails customUserDetails) {
        UserEntity user = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new ApiException(OrderError.USER_NOT_FOUND));

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
    /*주문 상세 정보 반환*/
    public OrderInfoDTO getOrderDetail(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(OrderError.ORDER_NOT_FOUND));

        return OrderInfoDTO.from(order);
    }
}

