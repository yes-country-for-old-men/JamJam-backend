package com.jamjam.notify;

import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.user.application.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fcm")
public class FcmController {
    private final FcmService fcmService;

    public FcmController (FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @PostMapping("/register")
    @Operation(summary = "FCM 토큰 저장")
    public ResponseEntity<ResponseDto<Void>> addFcmToken(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestBody FcmTokenRequest request) {
        fcmService.addFcmToken(customUserDetails, request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }
}
