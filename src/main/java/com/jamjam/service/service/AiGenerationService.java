package com.jamjam.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.global.exception.ApiException;
import com.jamjam.global.exception.CommonErrorCode;
import com.jamjam.service.dto.AiImageRequest;
import com.jamjam.service.dto.AiImageResponse;
import com.jamjam.service.dto.AiServiceRequest;
import com.jamjam.service.dto.AiServiceResponse;
import com.jamjam.service.util.OpenAiClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiGenerationService {
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public AiGenerationService(OpenAiClient openAiClient, ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
    }
    /*Gpt로부터 서비스명, 서비스 설명, 카테고리 요청 후 결과 반환*/
    public AiServiceResponse generateService(AiServiceRequest request) {
        /*gpt api 요청*/
        String content = openAiClient.requestGptForServiceElements(request);

        JsonNode node;
        try {
            // 전체 GPT 응답 파싱
            JsonNode full = objectMapper.readTree(content);
            // message.content 안에 실제 JSON 문자열이 있음
            String innerJsonString = full.path("choices").get(0).path("message").path("content").asText();
            // 다시 파싱 (중첩 JSON 구조이기 때문)
            node = objectMapper.readTree(innerJsonString);
        } catch (JsonProcessingException e) {
            throw new ApiException(CommonErrorCode.JSON_PROCESSING_ERROR);
        }

        List<String> serviceNames = new ArrayList<>();
        JsonNode namesNode = node.path("service_names");
        if (namesNode.isArray()) {
            for (JsonNode name : namesNode) {
                serviceNames.add(name.asText());
            }
        }

        String description = node.path("description").asText();
        int category = node.path("category").asInt();

        AiServiceResponse response = new AiServiceResponse();
        response.setServiceNames(serviceNames);
        response.setDescription(description);
        response.setCategory(category);

        return response;
    }
    /*이미지 프롬프트 생성 후
    * Gpt-image-1에 이미지 생성 요청*/
    public AiImageResponse generateImage(AiImageRequest request) {
        String imagePrompt;
        if (request.isTypography()) {
            imagePrompt = String.format(
                    "이 이미지는 정사각형 썸네일로, 중앙에는 \"%s\"라는 문구가 선명한 한글 타이포그래피로 배치되어 있습니다. " +
                            "전체 구도는 시각적으로 조화롭고 시선을 끌 수 있도록 구성되어야 합니다. " +
                            "이 서비스는 \"%s\"와 같은 특징을 가지고 있으므로, 이미지 분위기나 색감, 스타일은 이를 반영해야 합니다.",
                    request.getServiceName(),
                    request.getDescription()
            );
        } else {
            imagePrompt = String.format(
                    "이 이미지는 정사각형 썸네일입니다. 문구는 없어야 합니다." +
                            "전체 구도는 시각적으로 조화롭고 시선을 끌 수 있도록 구성되어야 합니다. " +
                            "이 서비스는 \"%s\"와 같은 특징을 가지고 있으므로, 이미지 분위기나 색감, 스타일은 이를 반영해야 합니다.",
                    request.getDescription()
            );
        }
        /*프론트에 ai 생성 결과를 보낼 때는 base64
        * 확정 후 DB에 저장 시 BLOB*/
        String b64Image = openAiClient.requestImageFromGptImage(imagePrompt, 1, "1024x1024");
        String thumbnailInfo = "data:image/png;base64," + b64Image;

        AiImageResponse response = new AiImageResponse();
        response.setImageBase64(thumbnailInfo);

        return response;
    }
}
