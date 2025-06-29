package com.jamjam.service.controller;

import com.jamjam.global.annotation.CurrentUser;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.service.dto.*;
import com.jamjam.service.service.AiGenerationService;
import com.jamjam.service.service.ServiceService;
import com.jamjam.user.application.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Provider;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/service")
public class ServiceController {
    private final AiGenerationService aiGenerationService;
    private final ServiceService serviceService;

    public ServiceController(AiGenerationService aiGenerationService, ServiceService serviceService) {
        this.aiGenerationService = aiGenerationService;
        this.serviceService = serviceService;
    }
    /*GPT에 서비스 명, 서비스 상세 설명, 카테고리 요청*/
    @PostMapping("/generate")
    @Operation(summary = "서비스 초안 생성", description = "gpt에 서비스 명, 서비스 상세 설명, 카테고리 요청")
    public ResponseEntity<ResponseDto<AiServiceResponse>> generateService(@RequestBody AiServiceRequest request) {
        AiServiceResponse response = aiGenerationService.generateService(request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
    /*Gpt-image-1에 썸네일 생성 요청*/
    @PostMapping("/ai-thumbnail")
    @Operation(summary = "ai 썸네일 생성 요청")
    public ResponseEntity<ResponseDto<AiImageResponse>> generateThumbnail(@RequestBody AiImageRequest request) {
        AiImageResponse response = aiGenerationService.generateImage(request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
    /*서비스 등록*/
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary ="서비스 등록")
    public ResponseEntity<ResponseDto<Void>> registerService(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestPart("request") ServiceRegisterRequest request,
            @RequestPart("thumbnail") MultipartFile thumbnail,
            @RequestPart(value = "portfolioImages", required = false) List<MultipartFile> portfolioImages) {
        serviceService.registerService(request, customUserDetails.getUserId(), thumbnail, portfolioImages);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.CREATE_SUCCESS));
    }
    /*서비스 목록 가져오기(카테고리, 제공자 별)*/
    @GetMapping("/service-list")
    @Operation(summary = "서비스 목록 조회", description = "카테고리, 제공자 별")
    public ResponseEntity<ResponseDto<Page<ServiceSummaryDTO>>> getServiceList(
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String provider,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ServiceSummaryDTO> responses = serviceService.getFilteredServices(category, provider, pageable);
        log.info("Service list response generated: totalElements={}, totalPages={}",
                responses.getTotalElements(), responses.getTotalPages());
        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, responses));
    }
    /*서비스 상세 페이지 조회*/
    @GetMapping("/detail")
    @Operation(summary = "서비스 상세 페이지 조회")
    public ResponseEntity<ResponseDto<ServiceInfoDTO>> getServiceDetail(
            @RequestParam UUID serviceId) {
        ServiceInfoDTO response = serviceService.getServiceDetail(serviceId);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
    /*서비스 삭제*/
    @DeleteMapping("/delete")
    @Operation(summary = "서비스 삭제")
    public ResponseEntity<ResponseDto<Void>> deleteService(
            @CurrentUser CustomUserDetails customUserDetails,
            @RequestParam UUID serviceId) {
        serviceService.deleteService(customUserDetails, serviceId);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.DELETE_SUCCESS));
    }
}
