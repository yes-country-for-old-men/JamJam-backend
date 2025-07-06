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
    public void changeStatusOrder(Long userId, OrderStatusRequest request) {
        OrderEntity order = verifyProvider(userId, request.getOrderId());

        order.changeStatus(request);

        orderRepository.save(order);
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
}

