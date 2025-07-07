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
    private Integer contactHoursStart;
    private Integer contactHoursEnd;
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
    @Getter @Builder
    public static class CareerDto {
        private Long id;
        private String company;
        private String position;
        private String department;
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;
        private Boolean freelancer;
        private String proofUrl;
    }
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