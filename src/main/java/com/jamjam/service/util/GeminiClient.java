package com.jamjam.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.global.exception.ApiException;
import com.jamjam.service.dto.gemini.Content;
import com.jamjam.service.dto.gemini.GeminiRequest;
import com.jamjam.service.dto.gemini.Part;
import com.jamjam.service.exception.ServiceError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
public class GeminiClient {

    private final WebClient geminiWebClient;
    private final String model;
    private final ObjectMapper objectMapper;

    public GeminiClient(
            @Qualifier("geminiWebClient") WebClient geminiWebClient,
            @Value("${gemini.api.model}") String model,
            ObjectMapper objectMapper) {
        this.geminiWebClient = geminiWebClient;
        this.model = model;
        this.objectMapper = objectMapper;
    }

    public JsonNode generateServiceInfo(String prompt) {
        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(
                        Content.builder()
                                .parts(List.of(
                                        Part.builder()
                                                .text(prompt)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        String responseBody;
        try {
            // TODO: 동기 처리 중, 비동기로 변환 필요
            responseBody = geminiWebClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .bodyValue(request)
                    .retrieve()
                    // 4xx, 5xx 에러 발생 시 처리
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Gemini 4xx Error: " + body)))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Gemini 5xx Error: " + body)))
                    .bodyToMono(String.class)
                    .block();

            log.info("[SERVICE] Gemini response: {}", responseBody);
        } catch (Exception e) {
            log.error("[GEMINI] API 요청 중 에러 발생", e);
            throw new ApiException(ServiceError.GEMINI_API_ERROR);
        }

        try {
            JsonNode root =  objectMapper.readTree(responseBody);
            String rawText =
                    root.at("/candidates/0/content/parts/0/text").asText();

            return objectMapper.readTree(rawText);
        } catch (JsonProcessingException e) {
            log.error("[GEMINI] Gemini 응답 JSON 파싱 실패.", e);
            throw new ApiException(ServiceError.JSON_PROCESSING_ERROR);
        }
    }
}
