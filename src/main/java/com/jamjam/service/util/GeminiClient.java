package com.jamjam.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.global.exception.ApiException;
import com.jamjam.service.dto.gemini.GeminiDto;
import com.jamjam.service.exception.ServiceError;
import com.jamjam.service.service.AIClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Primary
@Component
public class GeminiClient implements AIClient {

    private final WebClient geminiWebClient;
    private final String imageModel;
    private final String textModel;
    private final ObjectMapper objectMapper;

    public GeminiClient(
            @Qualifier("geminiWebClient") WebClient geminiWebClient,
            @Value("${gemini.api.image}") String imageModel,
            @Value("${gemini.api.text}") String textModel,
            ObjectMapper objectMapper) {
        this.geminiWebClient = geminiWebClient;
        this.imageModel = imageModel;
        this.textModel = textModel;
        this.objectMapper = objectMapper;
    }
    /* 텍스트 컨텐츠 생성 */
    @Override
    public JsonNode generateTextContent(String prompt) {
        GeminiDto.GeminiRequest request = buildGeminiRequest(prompt, "TEXT");

        String responseBody = callGeminiApi(request, "TEXT");

        try {
            JsonNode root =  objectMapper.readTree(responseBody);
            String rawString = root.at("/candidates/0/content/parts/0/text").asText();
            String cleanedString = santizeJsonString(rawString);

            return objectMapper.readTree(cleanedString);
        } catch (JsonProcessingException e) {
            log.error("[GEMINI] Gemini 응답 JSON 파싱 실패.", e);
            throw new ApiException(ServiceError.JSON_PROCESSING_ERROR);
        }
    }
    /* 이미지 컨텐츠 생성 */
    @Override
    public JsonNode generateImageContent(String prompt) {
        GeminiDto.GeminiRequest request = buildGeminiRequest(prompt, "IMAGE");

        String responseBody = callGeminiApi(request, "IMAGE");

        try {
            JsonNode root =  objectMapper.readTree(responseBody);
            JsonNode partsNode = root.at("/candidates/0/content/parts");

            if (partsNode.isArray()) {
                for (JsonNode part : partsNode) {
                    if (part.has("inlineData")) {
                        return part.get("inlineData");
                    }
                }
            }

            log.error("[GEMINI] 응답에 이미지 데이터가 없음. 전체 응답: {}", shorten(responseBody, 2000));
            throw new ApiException(ServiceError.GEMINI_API_ERROR);
        } catch (JsonProcessingException e) {
            log.error("[GEMINI] Gemini 응답 JSON 파싱 실패.", e);
            throw new ApiException(ServiceError.JSON_PROCESSING_ERROR);
        }
    }
    /* Gemini 요청 바디 생성
    * 파라미터:
    *  - 텍스트 생성: TEXT
    *  - 이미지 생성: IMAGE */
    private GeminiDto.GeminiRequest buildGeminiRequest(String prompt, String modality) {
        String mimeType = modality.equals("TEXT") ? "application/json" : null;

        return GeminiDto.GeminiRequest.builder()
                .contents(List.of(
                        GeminiDto.Content.builder()
                                .parts(List.of(
                                        GeminiDto.Part.builder()
                                                .text(prompt)
                                                .build()
                                ))
                                .build()
                ))
                .generationConfig(GeminiDto.GenerationConfig.builder()
                        .responseModalities(List.of(modality))
                        .responseMimeType(mimeType)
                        .build())
                .build();
    }
    /* Gemini Api 호출 */
    private String callGeminiApi(GeminiDto.GeminiRequest request, String type) {
        String model;
        if (type.equals("TEXT")) {
            model = textModel;
        } else {
            model = imageModel;
        }

        try {
            // TODO: 동기 처리 중, 비동기로 변환 필요
            return geminiWebClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .bodyValue(request)
                    .retrieve()
                    // [디버깅 1] 상태 코드 확인
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        log.error("[DEBUG] 2. API 상태 코드 에러 발생: {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Gemini API Error (" + clientResponse.statusCode() + "): " + body));
                    })
                    .bodyToMono(String.class)
                    // [디버깅 2] 구독 시작 (요청 날아감)
                    .doOnSubscribe(s -> log.info("[DEBUG] 3. WebClient 구독 시작 (요청 전송 중...)"))
                    // [디버깅 3] 데이터 수신 성공 시
                    .doOnNext(response -> log.info("[DEBUG] 4. 응답 수신 완료! (길이: {} bytes)", response.length()))
                    // [디버깅 4] 리액티브 체인 내부 에러 포착 (여기가 핵심!)
                    .doOnError(e -> log.error("[DEBUG] 💥 WebClient 내부 에러 감지!", e))
                    .block();
        } catch (Exception e) {
            log.error("[GEMINI] API 요청 중 에러 발생", e);
            throw new ApiException(ServiceError.GEMINI_API_ERROR);
        }
    }
    /* 응답 결과에 백틱 제거 */
    private String santizeJsonString(String rawString) {
        if (rawString == null) return "";

        return rawString
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
    }
    // TODO: 제거 필요
    private String shorten(String text, int maxLength) {
        if (text == null) return null;
        return text.length() <= maxLength
                ? text
                : text.substring(0, maxLength) + "...(truncated)";
    }
}
