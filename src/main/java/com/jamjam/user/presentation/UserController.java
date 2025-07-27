package com.jamjam.user.presentation;

import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.user.application.ReissueService;
import com.jamjam.user.application.SmsVerificationService;
import com.jamjam.user.application.UserService;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.domain.entity.CreditChangeType;
import com.jamjam.user.presentation.dto.request.*;
import com.jamjam.user.presentation.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public ResponseEntity<ResponseDto<UserResponse>> getUserInfo(
            @CurrentUser CustomUserDetails user
    ) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                userService.getUserInfo(user.getUserId())));
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<Void>> updateUserInfo(
            @CurrentUser CustomUserDetails user,
            @RequestPart("request") UserUpdateRequest request,
            @RequestPart(value = "profileUrl", required = false) MultipartFile profileImage) throws IOException {
        userService.updateUserInfo(user.getUserId(), request, profileImage);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ResponseDto<Map<String, String>>> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        return reissueService.reissueToken(request, response);
    }

    @PostMapping("/reissue/app")
    public ResponseEntity<ResponseDto<AppReissueResponse>> reissueApp(
            HttpServletRequest request
    ) {
        String newAccess = reissueService.reissueAppToken(request);
        AppReissueResponse response = new AppReissueResponse(newAccess, "Bearer");
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }

    @PostMapping("/join/provider")
    public ResponseEntity<ResponseDto<LoginResponse>> joinProvider(
            @RequestBody ProviderJoinRequest request,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            HttpServletResponse response
    ) {
        LoginResponse accessToken = userService.joinProvider(request, response, clientType);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, accessToken));
    }

    @PostMapping("/join/client")
    public ResponseEntity<ResponseDto<LoginResponse>> joinClient(
            @RequestBody ClientJoinRequest request,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType,
            HttpServletResponse response
    ) {
        LoginResponse accessToken = userService.joinClient(request, response, clientType);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, accessToken));
    }

    @GetMapping("/check/loginId")
    public ResponseEntity<ResponseDto<CheckDuplicateResponse>> checkDuplicateLoginId(
            @RequestParam String loginId
    ) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                userService.checkDuplicateLoginId(loginId)));
    }

    @GetMapping("/check/nickname")
    public ResponseEntity<ResponseDto<CheckDuplicateResponse>> checkDuplicateNickname(
            @RequestParam String nickname
    ) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                userService.checkDuplicateNickName(nickname)));
    }

    @PostMapping("/check-password")
    public ResponseEntity<ResponseDto<CheckCorrectResponse>> checkDuplicatePassword(
            @CurrentUser CustomUserDetails user,
            @RequestBody PasswordCheckRequest passwordRequest
    ) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                userService.checkCorrectPassword(user.getUserId(), passwordRequest.password())));
    }

    @GetMapping("/credit-history")
    @Operation(summary = "크레딧 사용 내역 조회")
    public ResponseEntity<ResponseDto<CreditHistoryResponse>> getCreditHistory (
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestParam CreditChangeType type,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(
                SuccessMessage.OPERATION_SUCCESS,
                userService.getCreditHistory(customUserDetails.getUserId(), type, pageable)));
    }
}
