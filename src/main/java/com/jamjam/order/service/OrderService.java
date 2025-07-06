package com.jamjam.order.service;

import com.jamjam.global.exception.ApiException;
import com.jamjam.order.domain.entity.OrderEntity;
import com.jamjam.order.domain.entity.OrderStatus;
import com.jamjam.order.domain.repository.OrderRepository;
import com.jamjam.order.dto.OrderRegisterRequest;
import com.jamjam.order.dto.OrderStatusRequest;
import com.jamjam.order.exception.OrderError;
import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.service.domain.repository.ServiceRepository;
import com.jamjam.service.util.S3Uploader;
import com.jamjam.user.domain.entity.UserEntity;
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

        Long clientId = order.getClient().getId();

        if (request.getOrderStatus() == OrderStatus.COMPLETED) {
            transferCreditOnConfirmation(clientId, providerId, order.getPrice());
        }
        //TODO: 구매 확정 대기로 변경 시 3일 뒤 자동 구매 확정으로 변경 && 구매 확정으로 변경 시, 크레딧 이동
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

        transferCreditOnConfirmation(userId, order.getService().getUser().getId(), order.getPrice());
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
    public void transferCreditOnConfirmation(Long clientId, Long providerId, BigDecimal price) {
        UserEntity client = userRepository.findById(clientId)
                .orElseThrow(() -> new ApiException(OrderError.USER_NOT_FOUND));
        UserEntity provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ApiException(OrderError.USER_NOT_FOUND));
        log.info("client: {}, provider: {}", client.getNickname(), provider.getNickname());

        client.changeCredit(price.negate());
        provider.changeCredit(price);
        log.info("client credit: {}, provider credit: {}", client.getCredit(), provider.getCredit());
        userRepository.save(client);
        userRepository.save(provider);
        log.info("크레딧 정산 완료");
    }
}

