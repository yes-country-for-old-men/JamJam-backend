package com.jamjam.order.controller;

import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.order.domain.entity.OrderStatus;
import com.jamjam.order.dto.*;
import com.jamjam.order.service.OrderService;
import com.jamjam.user.application.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @Operation(summary = "서비스 신청")
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
    @Operation(summary = "제공자가 주문의 상태를 변경")
    public ResponseEntity<ResponseDto<Void>> acceptOrder(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestBody OrderStatusRequest request) {
        orderService.changeStatusOrder(customUserDetails.getUserId(), request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.UPDATE_SUCCESS));
    }
    /*서비스 취소 - 구매자
    * 주문 수락 전에만 취소 가능*/
    @PatchMapping("/client/cancel")
    @Operation(summary = "구매자가 주문 수락 전 취소")
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
    @GetMapping("/order-list")
    @Operation(summary = "주문 내역")
    public ResponseEntity<ResponseDto<OrderListResponse>> getOrders(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestParam OrderStatus orderStatus,
            @PageableDefault(sort = "orderedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        OrderListResponse response = orderService.getOrders(customUserDetails, orderStatus, pageable);

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
}
