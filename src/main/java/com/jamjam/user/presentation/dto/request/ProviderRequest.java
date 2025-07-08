package com.jamjam.user.presentation.dto.request;

import com.jamjam.user.domain.entity.ContactHours;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record ProviderRequest(
    Integer categoryId,
    String location,
    String introduction,
    ContactHoursDto contactHours,
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
            String position
    ) {}
    @Builder
    public record EducationDto(Long id, String school, String major, String degree) {}
    @Builder
    public record LicenseDto(Long id, String name) {}
    @Builder
    public record ContactHoursDto(
            Integer startHour,
            Integer endHour
    ) {}
} 