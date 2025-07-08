package com.jamjam.order.service;

import com.jamjam.global.exception.ApiException;
import com.jamjam.order.domain.entity.OrderEntity;
import com.jamjam.order.domain.entity.OrderStatus;
import com.jamjam.order.domain.repository.OrderRepository;
import com.jamjam.order.dto.OrderInfoDTO;
import com.jamjam.order.dto.OrderRegisterRequest;
import com.jamjam.order.dto.OrderStatusRequest;
import com.jamjam.order.dto.OrderSummaryDTO;
import com.jamjam.order.exception.OrderError;
import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.service.domain.repository.ServiceRepository;
import com.jamjam.service.util.S3Uploader;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.entity.UserRole;
import com.jamjam.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
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

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, S3Uploader s3Uploader, ServiceRepository serviceRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.s3Uploader = s3Uploader;
        this.serviceRepository = serviceRepository;
    }
    /*주문 신청*/
    @Transactional
    public void registerService(OrderRegisterRequest request, Long userId, List<MultipartFile> images) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(OrderError.USER_NOT_FOUND));
        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ApiException(OrderError.SERVICE_NOT_FOUND));
        /*보유 크레딧과 주문 가격 비교*/
        if (user.getCredit().compareTo(request.getPrice()) < 0) throw new ApiException(OrderError.CREDIT_NOT_ENOUGH);

        user.changeCredit(request.getPrice().negate());
        log.info("client {} 크레딧 차감", request.getPrice());
        /*주문 내용 저장*/
        List<String> imageUrls = new ArrayList<>();
        try {
            if (images != null) {
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        String imageUrl = s3Uploader.upload(image, "order-request-images");
                        imageUrls.add(imageUrl);
                    }
                }
                log.info("참고 자료 이미지 저장 완료");
            }
        } catch (IOException e) {
            throw new ApiException(OrderError.IMAGE_UPLOAD_ERROR);
        }
        OrderEntity order = OrderEntity.builder()
                .title(request.getTitle())
                .deadline(request.getDeadline())
                .description(request.getDescription())
                .additionalRequest(request.getAdditionalRequest())
                .orderImages(imageUrls)
                .orderStatus(OrderStatus.REQUESTED)
                .price(request.getPrice())
                .client(user)
                .service(service)
                .build();
        orderRepository.save(order);
        log.info("서비스 신청 완료");
    }
    /*제공자의 주문 상태 변경*/
    @Transactional
    public void changeStatusOrder(Long providerId, OrderStatusRequest request) {
        OrderEntity order = verifyProvider(providerId, request.getOrderId());

        order.changeStatus(request);
        orderRepository.save(order);
        log.info("주문 상태 변경 완료");

        if (request.getOrderStatus() == OrderStatus.COMPLETED) {
            transferCreditOnConfirmation(providerId, order.getPrice());
        } else if (request.getOrderStatus() == OrderStatus.CANCELLED) {
            refundCreditOnCancellation(order.getClient(), order.getPrice());
        }
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
    public List<OrderSummaryDTO> getProviderOrders(CustomUserDetails customUserDetails, OrderStatus orderStatus) {
        UserEntity user = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new ApiException(OrderError.USER_NOT_FOUND));

        List<OrderEntity> orders = new ArrayList<>();
        if (user.getRole() == UserRole.PROVIDER) {
            orders = orderRepository.findByProviderIdAndOrderStatus(user.getId(), orderStatus);
        } else if (user.getRole() == UserRole.CLIENT) {
            orders = orderRepository.findByClientIdAndOrderStatus(user.getId(), orderStatus);
        }
        log.info("{} 상태 주문 건수: {}", orderStatus, orders.size());

        List<OrderSummaryDTO> selectedOrders = new ArrayList<>();
        for (OrderEntity order : orders) {
            selectedOrders.add(OrderSummaryDTO.from(order));
        }

        return selectedOrders;
    }
}

