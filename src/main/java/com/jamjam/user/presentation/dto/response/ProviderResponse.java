package com.jamjam.user.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ProviderResponse {
    private Integer categoryId;
    private String location;
    private String introduction;
    private ContactHoursDto contactHours;
    private String averageResponseTime;
    private List<SkillDto> skills;
    private List<CareerDto> careers;
    private List<EducationDto> educations;
    private List<LicenseDto> licenses;

    @Getter @Builder
    public static class SkillDto {
        private Long id;
        private String name;
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
            String position
    ) {}
    @Getter @Builder
    public static class EducationDto {
        private Long id;
        private String school;
        private String major;
        private String degree;
    }
    @Getter @Builder
    public static class LicenseDto {
        private Long id;
        private String name;
    }
} 