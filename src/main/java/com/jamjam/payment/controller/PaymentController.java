package com.jamjam.payment.controller;

import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.payment.exception.PaymentError;
import com.jamjam.payment.service.PortOneService;
import com.jamjam.payment.dto.CompletePaymentRequest;
import com.jamjam.payment.dto.PrepareOrderRequest;
import com.jamjam.user.application.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final PortOneService portOneService;

    public PaymentController(PortOneService portOneService) {
        this.portOneService = portOneService;
    }

    @PostMapping("/order")
    @Operation(summary = "결제 전 주문 내용 저장")
    public ResponseEntity<ResponseDto<Void>> prepareOrder(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestBody PrepareOrderRequest request) {
        portOneService.prepareOrder(customUserDetails, request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.CREATE_SUCCESS));
    }

    @PostMapping("/complete")
    @Operation(summary = "결제 정보 검증 후 처리")
    public ResponseEntity<ResponseDto<?>> completePayment(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestBody CompletePaymentRequest request) throws Exception {
        boolean result = portOneService.completePayment(customUserDetails, request);

        if (result) {
            return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.PAYMENT_COMPLETED));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseDto.ofFailure("INVALID_PAYMENT_DATA", "결제 금액이 주문과 일치하지 않아 환불 처리되었습니다."));
        }
    }

}
