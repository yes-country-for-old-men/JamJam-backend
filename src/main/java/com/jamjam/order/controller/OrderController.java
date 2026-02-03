package com.jamjam.order.controller;

import com.jamjam.chat.domain.entity.ChatMessageEntity;
import com.jamjam.chat.domain.entity.MessageType;
import com.jamjam.chat.presentation.dto.req.PaymentReq;
import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.order.domain.entity.OrderStatus;
import com.jamjam.order.dto.*;
import com.jamjam.order.exception.OrderError;
import com.jamjam.order.scheduler.OrderStatusScheduler;
import com.jamjam.order.service.OrderService;
import com.jamjam.user.application.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.http.PartMap;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /*서비스 신청*/
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "서비스 신청",
            description = """
                        message_type: REQUEST_FORM
                        content: {
                                    "serviceId": 1,
                                    "serviceName": "서비스명",
                                    "serviceThumbnail": "썸네일 URL",
                                    "orderId": 2
                                 }
                        
                        REQUESTED 상태로 주문이 생성됨.
                        """
    )
    public ResponseEntity<ResponseDto<Void>> registerOrder(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestPart("request") OrderRegisterRequest request,
            @RequestPart(value = "referenceFiles", required = false) List<MultipartFile> referenceFiles) {
        orderService.registerService(request, customUserDetails.getUserId(), referenceFiles);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.CREATE_SUCCESS));
    }
    /*서비스 상태 변경 - 제공자
    * 진행 중(수락), 취소, 완료됨 */
    @PatchMapping("/provider/change-status")
    @Operation(
            summary = "제공자가 주문의 상태를 변경",
            description = """
                        message_type: ORDER_CANCELLED or WORK_COMPLETED
                        content: {
                                    "serviceId": 1,
                                    "serviceName": "서비스명",
                                    "serviceThumbnail": "썸네일 URL",
                                    "orderId": 2
                                 }
                        
                        주문 상태 CANCELLED or WAITING_CONFIRM로 변경됨.
                        """
    )
    public ResponseEntity<ResponseDto<Void>> acceptOrder(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestBody OrderStatusRequest request) {
        orderService.changeStatusOrder(customUserDetails.getUserId(), request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.UPDATE_SUCCESS));
    }
    /*서비스 취소 - 구매자
    * 주문 수락 전에만 취소 가능*/
    @PatchMapping("/client/cancel")
    @Operation(
            summary = "구매자가 주문 수락 전 취소",
            description = """
                        message_type: ORDER_CANCELLED
                        content: {
                                    "serviceId": 1,
                                    "serviceName": "서비스명",
                                    "serviceThumbnail": "썸네일 URL",
                                    "orderId": 2
                                 }
                        
                        주문 상태 CANCELLED로 변경됨.
                        """
    )
    public ResponseEntity<ResponseDto<Void>> cancelPurchase(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestBody OrderStatusRequest request) {
        orderService.cancelPurchase(customUserDetails.getUserId(), request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.UPDATE_SUCCESS));
    }
    /*서비스 상태 변경 - 구매자
    * 구매 확정*/
    @PatchMapping("/client/{orderId}/confirm")
    @Operation(summary = "구매자가 구매 확정")
    public ResponseEntity<ResponseDto<Void>> confirmPurchase(
            @PathVariable Long orderId,
            @CurrentUser CustomUserDetails customUserDetails) {
        orderService.confirmPurchase(customUserDetails.getUserId(), orderId);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.UPDATE_SUCCESS));
    }
    /*주문 내역*/
    @GetMapping("/provider/order-list")
    @Operation(summary = "주문 내역")
    public ResponseEntity<ResponseDto<OrderListResponse>> getOrders(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestParam OrderStatus orderStatus,
            @PageableDefault(sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        OrderListResponse response = orderService.getProviderOrders(customUserDetails, orderStatus, pageable);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
    @GetMapping("/client/order-list")
    @Operation(summary = "주문 내역")
    public ResponseEntity<ResponseDto<OrderListResponse>> getOrders(
            @CurrentUser CustomUserDetails customUserDetails,
            @PageableDefault(sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        OrderListResponse response = orderService.getClientOrders(customUserDetails, pageable);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
    /*주문 상세 정보*/
    @GetMapping("/detail")
    @Operation(summary = "주문 상세 정보")
    public ResponseEntity<ResponseDto<OrderInfoDTO>> getOrderDetail(
            @RequestParam Long orderId) {
        OrderInfoDTO response = orderService.getOrderDetail(orderId);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
    /*주문 상태 별 개수 반환*/
    @GetMapping("/count")
    @Operation(summary = "주문 상태 별 개수")
    public ResponseEntity<ResponseDto<OrderCountResponse>> getOrderCount(
            @CurrentUser CustomUserDetails customUserDetails) {
        OrderCountResponse response = orderService.getOrderCount(customUserDetails);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
    @Operation(
            summary = "결제 요청",
            description = """
                        message_type: REQUEST_PAYMENT
                        content: "10000" //price가 String으로 전송됨 (JSON 아님)
                        """
    )
    @PostMapping("/request_payment")
    public ResponseEntity<ResponseDto<Void>> requestPayment(
            @Parameter(hidden = true) @CurrentUser CustomUserDetails user,
            @RequestBody PaymentReq request
    ) {
        orderService.requestPayment(user.getUserId(), request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }
    @Operation(
            summary = "결제",
            description = """
                        message_type: PAYMENT_COMPLETED
                        content: {
                                    "serviceId": 1,
                                    "serviceName": "서비스명",
                                    "serviceThumbnail": "썸네일 URL",
                                    "orderId": 2
                                 }
                        
                        주문 상태 PREPARING로 변경됨.
                        -> client는 이후 주문 취소 불가능
                        
                        차감된 client의 크레딧은 구매 확정 후 provider 크레딧에 추가
                        """

    )
    @PostMapping("/payment")
    public ResponseEntity<ResponseDto<Void>> processPayment(
            @Parameter(hidden = true) @CurrentUser CustomUserDetails user,
            @RequestBody PaymentReq request
    ) {
        orderService.processPayment(user.getUserId(), request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }
}
