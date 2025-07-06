package com.jamjam.order.controller;

import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.order.dto.OrderRegisterRequest;
import com.jamjam.order.dto.OrderStatusRequest;
import com.jamjam.order.scheduler.OrderStatusScheduler;
import com.jamjam.order.service.OrderService;
import com.jamjam.user.application.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
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
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        orderService.registerService(request, customUserDetails.getUserId(), images);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.CREATE_SUCCESS));
    }
    /*서비스 상태 변경 - 제공자
    * 진행 중(수락), 취소, 완료됨 */
    @PatchMapping("/change-status")
    @Operation(summary = "제공자가 주문의 상태를 변경")
    public ResponseEntity<ResponseDto<Void>> acceptOrder(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestBody OrderStatusRequest request) {
        orderService.changeStatusOrder(customUserDetails.getUserId(), request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

}
