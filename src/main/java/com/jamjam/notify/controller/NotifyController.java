package com.jamjam.notify.controller;

import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.notify.dto.NotificationSettingRequest;
import com.jamjam.notify.dto.NotificationSettingResponse;
import com.jamjam.notify.service.FcmService;
import com.jamjam.notify.dto.FcmTokenRequest;
import com.jamjam.notify.service.NotifyService;
import com.jamjam.user.application.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notify")
public class NotifyController {
    private final FcmService fcmService;
    private final NotifyService notifyService;

    public NotifyController(FcmService fcmService, NotifyService notifyService) {
        this.fcmService = fcmService;
        this.notifyService = notifyService;
    }

    @PostMapping("/fcm")
    @Operation(summary = "FCM 토큰 저장")
    public ResponseEntity<ResponseDto<Void>> addFcmToken(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestBody FcmTokenRequest request) {
        fcmService.addFcmToken(customUserDetails, request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }
    @PatchMapping("/setting")
    @Operation(summary = "사용자 알림 수신 여부 수정")
    public ResponseEntity<ResponseDto<Void>> changeNotificationSetting(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestBody NotificationSettingRequest request) {
        notifyService.changeNotificationSetting(customUserDetails, request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.UPDATE_SUCCESS));
    }
    @GetMapping("/setting")
    @Operation(summary = "사용자 알림 수신 여부 조회")
    public ResponseEntity<ResponseDto<NotificationSettingResponse>> getNotificationSetting(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestParam String device) {
        NotificationSettingResponse response = notifyService.getUserNotificationSetting(customUserDetails, device);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
}
