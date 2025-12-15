package com.jamjam.user.presentation.dto.response;

import com.jamjam.service.dto.ServiceInfoDTO;
import com.jamjam.service.dto.ServiceSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@Schema(description = "Provider 페이지 조회 응답")
public class ProviderPageResponse {
    @Schema(description = "카테고리 ID", example = "1")
    private Integer categoryId;

    @Schema(description = "지역", example = "서울")
    private String location;

    @Schema(description = "자기소개", example = "안녕하세요. 전문 개발자입니다.")
    private String introduction;

    @Schema(description = "연락 가능 시간")
    private ContactHoursDto contactHours;

    @Schema(description = "평균 응답 시간", example = "24시간 이내")
    private String averageResponseTime;

    @Schema(description = "보유 기술 목록")
    private List<SkillDto> skills;

    @Schema(description = "경력 목록")
    private List<CareerDto> careers;

    @Schema(description = "학력 목록")
    private List<EducationDto> educations;

    @Schema(description = "자격증 목록")
    private List<LicenseDto> licenses;

    @Schema(description = "프로필 이미지 URL")
    private String profileUrl;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "제공 서비스 목록")
    private List<ServiceSummaryDTO> services;

    @Getter @Builder
    @Schema(description = "기술 정보")
    public static class SkillDto {
        @Schema(description = "기술 ID", example = "1")
        private Long id;

        @Schema(description = "기술명", example = "Java")
        private String name;

        @Schema(description = "증빙자료 URL", example = "https://s3.amazonaws.com/jamjam2025/skills/abc123.pdf")
        private String proofUrl;
    }

    @Builder
    @Schema(description = "연락 가능 시간")
    public record ContactHoursDto(
            @Schema(description = "시작 시간 (0-23)", example = "9")
            Integer startHour,

            @Schema(description = "종료 시간 (0-23)", example = "18")
            Integer endHour
    ) {}

    @Builder
    @Schema(description = "경력 정보")
    public record CareerDto(
            @Schema(description = "경력 ID", example = "1")
            Long id,

            @Schema(description = "회사명", example = "네이버")
            String company,

            @Schema(description = "직책", example = "시니어 개발자")
            String position,

            @Schema(description = "증빙자료 URL", example = "https://s3.amazonaws.com/jamjam2025/careers/abc123.pdf")
            String proofUrl
    ) {}

    @Getter @Builder
    @Schema(description = "학력 정보")
    public static class EducationDto {
        @Schema(description = "학력 ID", example = "1")
        private Long id;

        @Schema(description = "학교명", example = "서울대학교")
        private String school;

        @Schema(description = "전공", example = "컴퓨터공학")
        private String major;

        @Schema(description = "학위", example = "학사")
        private String degree;

        @Schema(description = "증빙자료 URL", example = "https://s3.amazonaws.com/jamjam2025/educations/abc123.pdf")
        private String proofUrl;
    }

    @Getter @Builder
    @Schema(description = "자격증 정보")
    public static class LicenseDto {
        @Schema(description = "자격증 ID", example = "1")
        private Long id;

        @Schema(description = "자격증명", example = "정보처리기사")
        private String name;

        @Schema(description = "증빙자료 URL", example = "https://s3.amazonaws.com/jamjam2025/licenses/abc123.pdf")
        private String proofUrl;
    }
}