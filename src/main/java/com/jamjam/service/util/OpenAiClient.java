package com.jamjam.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.global.config.GptConfig;
import com.jamjam.global.exception.ApiException;
import com.jamjam.service.exception.ServiceError;
import com.jamjam.service.dto.AiServiceRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
                        "- 서비스 소개글: 실제 소개 페이지에서 고객이 구매 혹은 문의를 하도록 유도할 수 있어야 하며, 전문성과 감성을 함께 전달하는 마케팅용 소개글을 작성해줘. " +
                        "단순한 요약이나 정보 나열이 아니라, 신뢰를 줄 수 있는 말투와 고객이 어떤 가치를 얻는지 중심으로 이야기하듯 풀어줘.\n" +
                        "특히 다음 요소를 포함하도록 해줘:\n" +
                        "1. 시각적으로 구획이 잘 보이도록 HTML 태그를 적극적으로 활용해줘 (제목, 리스트, 구분선, 강조 등)\n" +
                        "2. 이모지도 적절하게 사용해줘\n" +
                        "3. 고객이 얻는 혜택, 감성, 경험 중심으로 표현해줘\n" +
                        "- 그리고 생성된 소개글 내용을 WYSIWYG 에디터용 HTML로 반환해줘. 단, HTML 태그 형식에 너무 갇히지 말고 글의 감동과 설득력을 우선해줘" +
                        "- 아래 리스트 중 하나의 카테고리 지정\n" +
                        "상세 설명: %s\n보유 기술: %s\n경력: %s\n" +
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
                        "\"반드시 코드 블록 없이 JSON 형식으로만 응답해줘.\"\n" +
                        "description 안에 포함된 모든 이모지(이모티콘)는 유니코드 이스케이프 형식(예: \\uD83D\\uDE00)으로 변환해서 반환해줘.\n" +
                        "JSON 양식은 다음과 같아." +
                        "{ \"service_names\": , \"description\": , \"category\": }",
                request.getDescription(),
                request.getSkills(),
                request.getCareer()
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
            throw new ApiException(ServiceError.JSON_PROCESSING_ERROR);
        }
    }
    /*서비스 상세 설명 받아 마크다운 문법 적용하여 반환*/
//    public String applyMarkdown(String description) {
//        String prompt = String.format(
//                "다음은 서비스 상세 설명 텍스트야. %s" +
//                        "이 내용을 사용자가 보기 좋도록 마크다운 문법을 적용해줘." +
//                        "JSON 형식으로만 응답해줘. 코드 블록 없이 말야.\n" +
//                        "반환 예시: \n" +
//                        "{\n \"appliedDescription\": \"여기에 마크다운 적용된 설명\"\n}",
//                description
//        );
//
//        Map<String, Object> body = new HashMap<>();
//        body.put("model", "gpt-4o");
//        body.put("temperature", 0.9);
//        body.put("max_tokens", 600);
//
//        List<Map<String, String>> messages = new ArrayList<>();
//        messages.add(Map.of("role", "system", "content", "너는 콘텐츠 마크다운 에디터야. 내가 제공하는 서비스 설명 텍스트를 마크다운 문법을 적용해서 사용자들이 읽기 쉽게 재작성해줘."));
//        messages.add(Map.of("role", "user", "content", prompt));
//        body.put("messages", messages);
//
//        String response = callOpenAI(GPT_URL, body);
//
//        JsonNode node;
//        try {
//            // 전체 GPT 응답 파싱
//            JsonNode full = objectMapper.readTree(response);
//            // message.content 안에 실제 JSON 문자열이 있음
//            String innerJsonString = full.path("choices").get(0).path("message").path("content").asText();
//            // 다시 파싱 (중첩 JSON 구조이기 때문)
//            node = objectMapper.readTree(innerJsonString);
//        } catch (JsonProcessingException e) {
//            throw new ApiException(ServiceError.JSON_PROCESSING_ERROR);
//        }
//        /*마크다운 적용된 상세 설명 반환*/
//        return node.path("appliedDescription").asText();
//    }
    private String callOpenAI(String url, Map<String, Object> requestBody) {
        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpEntity<String> entity = new HttpEntity<>(requestJson, gptConfig.httpHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            System.out.println("Open API 요청 성공");
            return response.getBody();
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패: {}", e.getMessage(), e);
            throw new ApiException(ServiceError.JSON_PROCESSING_ERROR);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // OpenAI에서 에러 반환 시
            log.error("OpenAI API 에러 - 상태 코드: {}, 응답 바디: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException(ServiceError.OPENAI_API_ERROR); // 커스텀 에러코드 사용
        }
    }



}