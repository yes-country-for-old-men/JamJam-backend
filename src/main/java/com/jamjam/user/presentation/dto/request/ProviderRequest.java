package com.jamjam.user.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.util.List;

@Builder
public record ProviderRequest(
    Integer categoryId,
    String location,
    String introduction,
    Integer contactHoursStart,
    Integer contactHoursEnd,
    String averageResponseTime,
    List<SkillDto> skills,
    List<CareerDto> careers,
    List<EducationDto> educations,
    List<LicenseDto> licenses
) {
    @Builder
    public record SkillDto(Long id, String name) {}
    @Builder
    public record CareerDto(
        Long id,
        String company,
        String position,
        String department,
        java.time.LocalDate startDate,
        java.time.LocalDate endDate,
        Boolean freelancer,
        String proofUrl
    ) {}
    @Builder
    public record EducationDto(Long id, String school, String major, String degree) {}
    @Builder
    public record LicenseDto(Long id, String name) {}
} 