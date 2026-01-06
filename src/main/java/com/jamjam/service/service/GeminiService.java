package com.jamjam.service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.service.dto.AiServiceRequest;
import com.jamjam.service.dto.AiServiceResponse;
import com.jamjam.service.util.GeminiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GeminiService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final PromptService promptService;

    public GeminiService(GeminiClient geminiClient, ObjectMapper objectMapper, PromptService promptService) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
        this.promptService = promptService;
    }

    // 서비스 세부내용 생성
    public AiServiceResponse generateService(Long userId, AiServiceRequest request) {
        String prompt = promptService.buildServicePrompt(userId, request.getDescription());
        log.info("[SERVICE] 프롬프트 생성 완료");

        JsonNode rawText = geminiClient.generateServiceInfo(prompt);

        List<String> serviceNames = objectMapper.convertValue(
                rawText.get("service_names"),
                new TypeReference<>() {}
        );
        String description = rawText.get("description").asText();
        int category = rawText.get("category").asInt();

        return new AiServiceResponse(serviceNames, description, category);
    }
}
