package com.jamjam.user.presentation;

import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.user.application.ReissueService;
import com.jamjam.user.application.SmsVerificationService;
import com.jamjam.user.application.UserService;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.presentation.dto.request.*;
import com.jamjam.user.presentation.dto.response.LoginResponse;
import com.jamjam.user.presentation.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final ReissueService reissueService;
    private final SmsVerificationService smsVerificationService;

    public UserController(UserService userService, ReissueService reissueService, SmsVerificationService smsVerificationService) {
        this.userService = userService;
        this.reissueService = reissueService;
        this.smsVerificationService = smsVerificationService;
    }

    @PostMapping("/sms/send")
    public ResponseEntity<ResponseDto<Void>> sendOne(@RequestBody SmsSendRequest request) throws Exception {
        smsVerificationService.sendMessage(request.phoneNumber());
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @PostMapping("/sms/verify")
    public ResponseEntity<ResponseDto<Void>> verifyOne(@RequestBody SmsVerifyRequest request) {
        smsVerificationService.verifyCode(request.phoneNumber(), request.code());
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @GetMapping
    public ResponseEntity<ResponseDto<UserResponse>> getUserInfo(@CurrentUser CustomUserDetails user) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                userService.getUserInfo(user.getUserId())));
    }

    @PatchMapping
    public ResponseEntity<ResponseDto<Void>> updateUserInfo(@CurrentUser CustomUserDetails user, @RequestBody UserUpdateRequest request) {
        userService.updateUserInfo(user.getUserId(), request);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ResponseDto<Map<String, String>>> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        return reissueService.reissueToken(request, response);
    }

    @PostMapping("/join/provider")
    public ResponseEntity<ResponseDto<LoginResponse>> joinProvider(
            @RequestBody ProviderJoinRequest request,
            HttpServletResponse response
    ) {
        LoginResponse accessToken = userService.joinProvider(request, response);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, accessToken));
    }

    @PostMapping("/join/client")
    public ResponseEntity<ResponseDto<LoginResponse>> joinClient(
            @RequestBody ClientJoinRequest request,
            HttpServletResponse response
    ) {
        LoginResponse accessToken = userService.joinClient(request, response);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, accessToken));
    }

    @GetMapping("/check-loginId")
    public ResponseEntity<ResponseDto<Boolean>> checkDuplicateLoginId(@RequestParam String loginId) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS
                , userService.checkDuplicateLoginId(loginId)));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<ResponseDto<Boolean>> checkDuplicateNickname(@RequestParam String nickName) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS
                , userService.checkDuplicateNickName(nickName)));
    }
}
