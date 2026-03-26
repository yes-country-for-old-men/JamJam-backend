package com.jamjam.payment.service;

import com.jamjam.global.exception.ApiException;
import com.jamjam.payment.domain.entity.CreditOrderEntity;
import com.jamjam.payment.domain.entity.PaymentEntity;
import com.jamjam.payment.domain.entity.PaymentStatus;
import com.jamjam.payment.domain.repository.CreditOrderRepository;
import com.jamjam.payment.domain.repository.PaymentRepository;
import com.jamjam.payment.dto.CompletePaymentRequest;
import com.jamjam.payment.dto.PrepareOrderRequest;
import com.jamjam.payment.exception.PaymentError;
import com.jamjam.payment.util.PortOneApiClient;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.domain.entity.CreditChangeType;
import com.jamjam.user.domain.entity.CreditHistoryEntity;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.repository.CreditHistoryRepository;
import com.jamjam.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class PortOneService {
    private final PortOneApiClient portOneApiClient;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CreditOrderRepository creditOrderRepository;
    private final CreditHistoryRepository creditHistoryRepository;

    public PortOneService(PortOneApiClient portOneApiClient, PaymentRepository paymentRepository,
                          UserRepository userRepository, CreditOrderRepository creditOrderRepository, CreditHistoryRepository creditHistoryRepository) {
        this.portOneApiClient = portOneApiClient;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.creditOrderRepository = creditOrderRepository;
        this.creditHistoryRepository = creditHistoryRepository;
    }

    /*결제 전, 주문 정보 저장*/
    @Transactional
    public void prepareOrder(CustomUserDetails customUserDetails, PrepareOrderRequest request) {
        Long userId = customUserDetails.getUserId();
        log.info("userId: {}", userId);
        UserEntity user =
                userRepository.findByIdOrThrow(customUserDetails.getUserId(), PaymentError.USER_NOT_FOUND);

        /*사전 결제 내용 저장
        * 결제 상태: 준비*/
        PaymentEntity prePayment = PaymentEntity.builder()
                .status(PaymentStatus.READY)
                .paymentUid(request.getPaymentUid())
                .build();
        paymentRepository.save(prePayment);
        /*주문 내용 저장
        * 결제 승인 시, 아래 객체의 금액과
        * 결제 정보의 금액을 통해 검증*/
        CreditOrderEntity preOrder = CreditOrderEntity.builder()
                .price(request.getPrice())
                .user(user)
                .payment(prePayment)
                .build();
        creditOrderRepository.save(preOrder);
    }
    /*결제 검증 후, 처리
    * 검증 성공 시, 크레딧 충전*/
    @Transactional
    public boolean completePayment(CustomUserDetails customUserDetails, CompletePaymentRequest request) {
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
        CreditOrderEntity order = creditOrderRepository.findByPaymentPaymentUid(request.getPaymentUid())
                .orElseThrow(() -> new ApiException(PaymentError.ORDER_NOT_FOUND));
        BigDecimal orderPrice = order.getPrice();

        PaymentEntity payment = order.getPayment();
        /*가상 계좌 결제는 미포함*/
        if ("PAID".equals(status) && orderPrice.compareTo(paidPrice) == 0) {
            /*결제 완료 인증되면
            * payment 객체의 status: OK, paymentUid: 포트원 결제 아이디
            * 로 변경*/
            payment.changePaymentBySuccess(PaymentStatus.OK, request.getPaymentUid());
            paymentRepository.save(payment);

            UserEntity user = userRepository.findWithLockById(customUserDetails.getUserId())
                    .orElseThrow(() -> new ApiException(PaymentError.USER_NOT_FOUND));
            user.addCredit(orderPrice);
            userRepository.save(user);
            log.info("{} 크레딧 충전 완료", orderPrice);

            CreditHistoryEntity chargeHistory = CreditHistoryEntity.builder()
                    .amount(orderPrice)
                    .type(CreditChangeType.DEPOSIT)
                    .reason("크레딧 결제")
                    .user(user)
                    .build();
            creditHistoryRepository.save(chargeHistory);
            log.info("충전 내역 저장");

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
            creditOrderRepository.delete(order);
            paymentRepository.delete(payment);
            return false;
        }
    }
}
