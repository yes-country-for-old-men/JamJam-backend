package com.jamjam.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.config.GptConfig;
import com.jamjam.global.exception.ApiException;
import com.jamjam.global.exception.CommonErrorCode;
import com.jamjam.service.dto.AiServiceRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class OpenAiClient {
    private final GptConfig gptConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String GPT_URL = "https://api.openai.com/v1/chat/completions";
    private final String GPT_IMAGE_URL = "https://api.openai.com/v1/images/generations";

    public OpenAiClient(GptConfig gptConfig, RestTemplate restTemplate) {
        this.gptConfig = gptConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String requestGptForServiceElements(AiServiceRequest request) {
        //TODO: 카테고리 확정 시 적용해야함
        String prompt = String.format(
                "다음 정보를 기반으로 서비스명, 설명 요약, 적절한 카테고리, 이미지 생성용 프롬프트 요소를 생성해줘:\n" +
                        "상세 설명: %s\n보유 기술: %s\n경력: %s\n학력 및 자격증: %s\n\n" +
                        "\"JSON 형식으로만 응답해줘. 코드 블록 없이 말야.\"\n" +
                        "JSON 형식 예시:\n" +
                        "{\n" +
                        "  \"service_name\": \"감성 캘리그라피\",\n" +
                        "  \"category\": \"디자인\",\n" +
                        "  \"summary\": \"손글씨로 마음을 전하는 캘리그라피 서비스\",\n" +
                        "  \"visual_elements\": \"따뜻한 우드톤 배경에 손글씨를 쓰는 장면\",\n" +
                        "  \"tone_style\": \"부드럽고 감성적인 분위기\",\n" +
                        "  \"typography_style\": \"손글씨\"\n" +
                        "}",
                request.getDescription(),
                request.getSkills(),
                request.getCareer(),
                request.getEducation()
        );

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o");
        body.put("temperature", 0.9);
        body.put("max_tokens", 600);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "너는 노인들의 특기와 경력을 기반으로 외주 서비스를 구성하는 어시스턴트야. 시각 요소와 썸네일 구성도 포함해."));
        messages.add(Map.of("role", "user", "content", prompt));
        body.put("messages", messages);

        return callOpenAI(GPT_URL, body);
    }

    public String requestImageFromGptImage(String prompt, int n, String size) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-image-1");
        body.put("prompt", prompt);
        body.put("n", n);
        body.put("size", size);

        String response = callOpenAI(GPT_IMAGE_URL, body);
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("data").get(0).path("b64_json").asText();
        } catch (Exception e) {
            throw new ApiException(CommonErrorCode.JSON_PROCESSING_ERROR);
        }
    }

    private String callOpenAI(String url, Map<String, Object> requestBody) {
        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpEntity<String> entity = new HttpEntity<>(requestJson, gptConfig.httpHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            return response.getBody();
        } catch (JsonProcessingException e) {
            throw new ApiException(CommonErrorCode.JSON_PROCESSING_ERROR);
        }
    }
}