package com.jamjam.service.service;

import com.jamjam.global.exception.ApiException;
import com.jamjam.service.dto.AiImageRequest;
import com.jamjam.service.exception.ServiceError;
import com.jamjam.user.domain.entity.ProviderEntity;
import com.jamjam.user.domain.entity.SkillEntity;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.repository.ProviderRepository;
import com.jamjam.user.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class PromptService {

    private final ProviderRepository providerRepository;

    public PromptService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    // 서비스 내용 생성 프롬프트 빌드
    public String buildServicePrompt(Long userId, String description) {
        ProviderEntity provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ServiceError.PROVIDER_NOT_FOUND));

        List<String> skills = provider.getSkills().stream()
                .map(SkillEntity::getName)
                .filter(Objects::nonNull)
                .toList();

        List<String> careers = provider.getCareers().stream()
                .map(c -> {
                    String company = Optional.ofNullable(c.getCompany()).orElse("회사명 없음");
                    String position = Optional.ofNullable(c.getPosition()).orElse("직무 없음");
                    return company + "-" + position;
                })
                .toList();

        return String.format(
                "다음 정보를 기반으로 아래 조건에 맞는 항목들을 생성해줘:\n" +
                        "- 서비스명 3가지 제안\n" +
                        "- 서비스 소개글: 실제 소개 페이지에서 고객이 구매 혹은 문의를 하도록 유도할 수 있어야 하며, 전문성과 감성을 함께 전달하는 마케팅용 소개글을 작성해줘. " +
                        "특히 다음 요소를 꼭 반영해줘:\n" +
                        "1. 시각적으로 구획이 잘 보이도록 HTML 태그를 적극적으로 활용해줘 (제목, 리스트, 구분선, 강조 등)\n" +
                        "2. 이모지도 적절하게 사용해줘\n" +
                        "3. 고객이 얻는 혜택, 감성, 경험 중심으로 표현해줘\n" +
                        "4. 단순 나열이 아닌, 말하듯 풀어서 이야기하는 형식\n" +
                        "5. 고객 페르소나를 상정해서 그들이 공감할 수 있도록 써줘\n" +
                        "6. 총 분량은 800자 이상, 그리고 입력으로 들어오는 서비스 소개글의 3배 분량은 최소한 만들어줘.\n" +
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
                        "{ \"service_names\": [...], \"description\": \"...\", \"category\": 6 }",
                description,
                skills,
                careers
        );
    }
    // 썸네일 디자인 요소 추출용 프롬프트 생성
    public String buildDesignElementPrompt(String description, String serviceName) {
        return String.format(
                "외주 서비스 분야와 메인 문구를 바탕으로 시각 요소를 제안해줘.\n" +
                        "서비스 분야: %s\n" +
                        "메인 문구: %s\n" +
                        "아래 세 가지 요소만 코드 블럭 없이 JSON 형식으로 생성해줘.\n" +
                        "1. visual_elements: 배경과 주변에 배치된 시각 요소 (한 문장)\n" +
                        "2. tone_style: 전체 분위기와 스타일 (예: 따뜻하고 빈티지한 느낌)\n" +
                        "3. typography_style: 타이포그래피 느낌 (예: 손글씨, 산세리프, 고딕체 등)\n" +
                        "JSON 형식 예시:\n" +
                        "{\n" +
                        "\"visual_elements\":\"우드톤 책상 위에 놓인 향기 나는 커피잔과 노트북\",\n" +
                        "\"tone_style\":\"차분하고 전문적인 블로그 느낌\",\n" +
                        "\"typography_style\":\"모던한 산세리프\"\n" +
                        "}",
                description,
                serviceName
        );
    }
}
