package com.jamjam.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.global.dto.ResponseDto;
import com.jamjam.global.dto.SuccessMessage;
import com.jamjam.service.dto.AiServiceRequest;
import com.jamjam.service.dto.AiServiceResponse;
import com.jamjam.service.service.AiGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service")
public class ServiceController {
    private final AiGenerationService aiGenerationService;

    public ServiceController(AiGenerationService aiGenerationService, ObjectMapper objectMapper) {
        this.aiGenerationService = aiGenerationService;
    }
    /*GPT에 서비스 명, 서비스 상세 설명, 카테고리 요청*/
    @PostMapping("/generate")
    public ResponseEntity<ResponseDto<AiServiceResponse>> generateService(@RequestBody AiServiceRequest request) {
        AiServiceResponse response = aiGenerationService.generateService(request);

        return ResponseEntity.ok(ResponseDto.ofSuccess(SuccessMessage.OPERATION_SUCCESS, response));
    }
}
