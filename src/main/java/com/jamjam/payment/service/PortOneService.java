package com.jamjam.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.global.exception.ApiException;
import com.jamjam.payment.domain.entity.OrderEntity;
import com.jamjam.payment.domain.entity.PaymentEntity;
import com.jamjam.payment.domain.entity.PaymentStatus;
import com.jamjam.payment.domain.repository.OrderRepository;
import com.jamjam.payment.domain.repository.PaymentRepository;
import com.jamjam.payment.dto.CompletePaymentRequest;
import com.jamjam.payment.dto.PrepareOrderRequest;
import com.jamjam.payment.exception.PaymentError;
import com.jamjam.payment.util.PortOneApiClient;
import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.service.domain.repository.ServiceRepository;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class PortOneService {
    private final PortOneApiClient portOneApiClient;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ServiceRepository serviceRepository;


    public PortOneService(PortOneApiClient portOneApiClient, PaymentRepository paymentRepository,
                          UserRepository userRepository, OrderRepository orderRepository, ServiceRepository serviceRepository) {
        this.portOneApiClient = portOneApiClient;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.serviceRepository = serviceRepository;
    }

    /*결제 전, 주문 정보 저장*/
    public void prepareOrder(CustomUserDetails customUserDetails, PrepareOrderRequest request) {
        Long userId = customUserDetails.getUserId();
        log.info("userId: {}", userId);
        UserEntity user = userRepository.findById(customUserDetails.getUserId())
                .orElseThrow(() -> new ApiException(PaymentError.USER_NOT_FOUND));
        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ApiException(PaymentError.SERVICE_NOT_FOUND));

        /*사전 결제 내용 저장
        * 결제 상태: 준비*/
        PaymentEntity prePayment = PaymentEntity.builder()
                .status(PaymentStatus.READY)
                .build();

        paymentRepository.save(prePayment);
        /*주문 내용 저장
        * 결제 승인 시, 아래 객체의 금액과
        * 결제 정보의 금액을 통해 검증*/
        OrderEntity preOrder = OrderEntity.builder()
                .price(request.getPrice())
                .merchantUid(request.getMerchantUid())
                .user(user)
                .service(service)
                .payment(prePayment)
                .build();

        orderRepository.save(preOrder);
    }
    /*결제 검증 후, 처리*/
    public boolean completePayment(CompletePaymentRequest request) {
        /*포트원 단건 조회 API 호출*/
        Map<String, Object> paymentDetails;
        log.info("PortOne 단건 조회 API 호출");
        try {
            paymentDetails = portOneApiClient.getPaymentDetails(request.getPaymentUid());
            log.info("결제 정보 조회 성공");
        } catch (Exception e) {
            log.error("결제 정보 조회 실패", e);
            throw new ApiException(PaymentError.GET_PAYMENT_FAILED);
        }
        String status = (String) paymentDetails.get("status");
        log.info(status);
        /*실제 결제된 금액*/
        Map<String, Object> amountDetails = (Map<String, Object>) paymentDetails.get("amount");
        BigDecimal paidPrice = new BigDecimal((Integer) amountDetails.get("total"));
        log.info("paidPrice: {}", paidPrice);
        /*주문서 금액*/
        OrderEntity order = orderRepository.findByMerchantUid(request.getMerchantUid())
                .orElseThrow(() -> new ApiException(PaymentError.ORDER_NOT_FOUND));
        BigDecimal orderPrice = order.getPrice();

        PaymentEntity payment = order.getPayment();
        //TODO: 가상계좌 결제는 포함X
        if ("PAID".equals(status) && orderPrice.compareTo(paidPrice) == 0) {
            /*결제 완료 인증되면
            * payment 객체의 status: OK, paymentUid: 포트원 결제 아이디
            * 로 변경*/
            payment.changePaymentBySuccess(PaymentStatus.OK, request.getPaymentUid());
            paymentRepository.save(payment);
            return true;
        } else {
            /*결제 정보 다르면,
            * 결제 취소, DB에서 order, payment 객체 삭제*/
            log.error("결제 정보가 다릅니다.");
            try {
                portOneApiClient.cancelPayment(request.getPaymentUid());
                log.info("포트원 결제 취소 성공");
            } catch (Exception e) {
                log.error("자동 환불 실패", e);
                throw new ApiException(PaymentError.REFUND_FAILED);
            }
            orderRepository.delete(order);
            paymentRepository.delete(payment);
            return false;
        }
    }
}
