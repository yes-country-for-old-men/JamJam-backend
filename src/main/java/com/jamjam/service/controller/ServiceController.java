package com.jamjam.service.controller;

import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.service.dto.*;
import com.jamjam.service.service.AiGenerationService;
import com.jamjam.service.service.ServiceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    public ResponseEntity<ResponseDto<AiServiceResponse>> generateService(@RequestBody AiServiceRequest request) {
        AiServiceResponse response = aiGenerationService.generateService(request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
    /*Gpt-image-1에 썸네일 생성 요청*/
    @PostMapping("/ai-thumbnail")
    public ResponseEntity<ResponseDto<AiImageResponse>> generateThumbnail(@RequestBody AiImageRequest request) {
        AiImageResponse response = aiGenerationService.generateImage(request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
    /*서비스 등록*/
    @PostMapping("/register")
    public ResponseEntity<ResponseDto<Void>> registerService(
            @RequestPart("request") ServiceRegisterRequest request,
            @RequestPart("thumbnail")MultipartFile thumbnail,
            @RequestPart(value = "infoImages", required = false) List<MultipartFile> infoImages) {
        serviceService.registerService(request, thumbnail, infoImages);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.CREATE_SUCCESS));
    }
}
