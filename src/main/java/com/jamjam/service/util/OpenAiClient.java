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
    /*최초 입력에 대한 1차 출력
    * 서비스명 3개, 서비스 설명, 카테고리 추출*/
    public String requestGptForServiceElements(AiServiceRequest request) {
        String prompt = String.format(
                "다음 정보를 기반으로 아래 조건에 맞는 항목들을 생성해줘:\n" +
                        "- 서비스명 3가지 제안\n" +
                        "- 서비스 소개글 (설명 요약이 아니라 실제 소개 페이지에 들어갈 수 있는 풍부한 소개글로 작성. 입력된 정보를 기반으로 살을 붙여도 좋아.)\n" +
                        "- 아래 리스트 중 하나의 카테고리 지정\n" +
                        "상세 설명: %s\n보유 기술: %s\n경력: %s\n학력 및 자격증: %s\n" +
                        "카테고리는 아래 중에서 하나만 골라줘 (그 외의 값은 넣지 마. 오른쪽의 ID 값으로 반환해줘):\n" +
                        "- BUSINESS: 1\n" +
                        "- CONSULTING: 2\n" +
                        "- MARKETING: 3\n" +
                        "- DEVELOPMENT: 4\n" +
                        "- DESIGN: 5\n" +
                        "- WRITE: 6\n" +
                        "- TRANSLATION: 7\n" +
                        "- PHOTOGRAPH: 8\n" +
                        "- EDUCATION: 9\n" +
                        "- CRAFT: 10\n" +
                        "- HOBBY: 11\n" +
                        "- LIVING: 12\n" +
                        "\"JSON 형식으로만 응답해줘. 코드 블록 없이 말야.\"\n" +
                        "JSON 응답 예시:\n" +
                        "{\n" +
                        "  \"service_names\": [\"감성 캘리그라피\", \"손글씨 엽서 제작\", \"따뜻한 문구 디자인\"],\n" +
                        "  \"description\": \"감성적인 손글씨를 활용한 맞춤형 디자인 서비스를 제공합니다.\",\n" +
                        "  \"category\": 5\n" +
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
    /*썸네일 이미지 생성*/
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