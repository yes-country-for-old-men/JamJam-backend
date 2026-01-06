package com.jamjam.service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.service.dto.AiImageRequest;
import com.jamjam.service.dto.AiImageResponse;
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

    /* 서비스 상세내용 생성 */
    public AiServiceResponse generateService(Long userId, AiServiceRequest request) {
        String prompt = promptService.buildServicePrompt(userId, request.getDescription());
        log.info("[SERVICE] 서비스 세부내용 프롬프트 생성 완료");

        JsonNode rawText = geminiClient.generateTextContent(prompt);

        List<String> serviceNames = objectMapper.convertValue(
                rawText.get("service_names"),
                new TypeReference<>() {}
        );
        String description = rawText.get("description").asText();
        int category = rawText.get("category").asInt();

        return new AiServiceResponse(serviceNames, description, category);
    }
    /* 썸네일 생성 */
    public void generateThumbnail(AiImageRequest request) {
        /* 디자인 요소 추출 */
        String designElementPrompt = promptService.buildDesignElementPrompt(request.getDescription(), request.getServiceName());
        log.info("[SERVICE] 디자인 요소 프롬프트 생성 완료");

        JsonNode rawText = geminiClient.generateTextContent(designElementPrompt);

        String visualElements = rawText.get("visual_elements").asText();
        String toneStyle = rawText.get("tone_style").asText();
        String typographyStyle = rawText.get("typography_style").asText();

        log.info("[SERVICE] visual elements: {}", visualElements);
        log.info("[SERVICE] tone style: {}", toneStyle);
        log.info("[SERVICE] typography style: {}", typographyStyle);

        /* 디자인 요소 기반 썸네일 생성 */
    }
}
