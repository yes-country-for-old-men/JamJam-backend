package com.jamjam.user.presentation.dto.response;

import com.jamjam.service.dto.ServiceInfoDTO;
import com.jamjam.service.dto.ServiceSummaryDTO;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ProviderPageResponse {
    private Integer categoryId;
    private String location;
    private String introduction;
    private ContactHoursDto contactHours;
    private String averageResponseTime;
    private List<SkillDto> skills;
    private List<CareerDto> careers;
    private List<EducationDto> educations;
    private List<LicenseDto> licenses;
    private String profileUrl;
    private String nickname;
    private List<ServiceSummaryDTO> services;

    @Getter @Builder
    public static class SkillDto {
        private Long id;
        private String name;
        private String proofUrl;
    }
    @Builder
    public record ContactHoursDto(
            Integer startHour,
            Integer endHour
    ) {}
    @Builder
    public record CareerDto(
            Long id,
            String company,
            String position,
            String proofUrl
    ) {}
    @Getter @Builder
    public static class EducationDto {
        private Long id;
        private String school;
        private String major;
        private String degree;
        private String proofUrl;
    }
    @Getter @Builder
    public static class LicenseDto {
        private Long id;
        private String name;
        private String proofUrl;
    }
}