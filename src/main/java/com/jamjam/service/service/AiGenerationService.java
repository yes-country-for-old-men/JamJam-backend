package com.jamjam.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jamjam.global.exception.ApiException;
import com.jamjam.global.exception.CommonErrorCode;
import com.jamjam.service.dto.AiServiceRequest;
import com.jamjam.service.dto.AiServiceResponse;
import com.jamjam.service.util.OpenAiClient;
import org.springframework.stereotype.Service;

@Service
public class AiGenerationService {
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    public AiGenerationService(OpenAiClient openAiClient, ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.objectMapper = objectMapper;
    }
    /*ai로부터 썸네일을 포함한 서비스 초안 받아오기*/
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

        String serviceName = node.path("service_name").asText();
        String category = node.path("category").asText();
        String description = node.path("summary").asText();
        String visualElements = node.path("visual_elements").asText();
        String toneStyle = node.path("tone_style").asText();
        String typographyStyle = node.path("typography_style").asText();
        /*이미지 프롬프트 생성*/
        String imagePrompt = String.format(
                "%s. 이미지 중앙에는 \"%s\"라는 문구가 %s 스타일의 한글 타이포그래피로 선명하고 정확하게 배치되어 있으며, %s 분위기의 정사각형 썸네일입니다. 전체 구도는 시각적으로 조화롭고 집중을 끌 수 있게 설계되어야 합니다.",
                visualElements, serviceName, typographyStyle, toneStyle
        );
        /*프론트에 ai 생성 결과를 보낼 때는 base64
        * 확정 후 DB에 저장 시 BLOB*/
        String b64Image = openAiClient.requestImageFromGptImage(imagePrompt, 1, "1024x1024");
        String thumbnailInfo = "data:image/png;base64," + b64Image;

        AiServiceResponse response = new AiServiceResponse();
        response.setServiceName(serviceName);
        response.setCategory(category);
        response.setDescription(description);
        response.setThumbnailInfo(thumbnailInfo);
        response.setSalary(request.getSalary());
        response.setSkills(request.getSkills());
        response.setCareer(request.getCareer());
        response.setEducation(request.getEducation());

        return response;
    }
}
