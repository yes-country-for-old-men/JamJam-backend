package com.jamjam.user.presentation;

import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.user.application.ProviderService;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.domain.entity.ProviderEntity;
import com.jamjam.user.presentation.dto.request.ProviderRequest;
import com.jamjam.user.presentation.dto.response.ProviderResponse;
import com.jamjam.user.presentation.dto.response.ProviderPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {
    private final ProviderService providerService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<Void>> createProvider(
            @CurrentUser CustomUserDetails user,
            @RequestPart("request") ProviderRequest request,
            @RequestPart(value = "skillFiles", required = false) List<MultipartFile> skillFiles,
            @RequestPart(value = "careerFiles", required = false) List<MultipartFile> careerFiles,
            @RequestPart(value = "educationFiles", required = false) List<MultipartFile> educationFiles,
            @RequestPart(value = "licenseFiles", required = false) List<MultipartFile> licenseFiles
    ) throws IOException {
        providerService.createProvider(user.getUserId(), request, skillFiles, careerFiles, educationFiles, licenseFiles);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }

    @GetMapping
    public ResponseEntity<ResponseDto<ProviderResponse>> getProvider(
            @CurrentUser CustomUserDetails user
    ) {
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS,
                providerService.getProvider(user.getUserId())));
    }

    @GetMapping("/page/{userId}")
    public ResponseEntity<ResponseDto<ProviderPageResponse>> getProviderPage(
            @PathVariable Long userId
    ) {
        ProviderPageResponse response = providerService.getProviderPage(userId);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<ProviderResponse>> updateProvider(
            @CurrentUser CustomUserDetails user,
            @RequestPart("request") ProviderRequest request,
            @RequestPart(value = "skillFiles", required = false) List<MultipartFile> skillFiles,
            @RequestPart(value = "careerFiles", required = false) List<MultipartFile> careerFiles,
            @RequestPart(value = "educationFiles", required = false) List<MultipartFile> educationFiles,
            @RequestPart(value = "licenseFiles", required = false) List<MultipartFile> licenseFiles
    ) throws IOException {
        ProviderResponse response = providerService.updateProvider(user.getUserId(), request, skillFiles, careerFiles, educationFiles, licenseFiles);
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }

    @DeleteMapping
    public ResponseEntity<ResponseDto<Void>> deleteProvider(
            @CurrentUser CustomUserDetails user
    ) {
        providerService.deleteProvider(user.getUserId());
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS));
    }
}