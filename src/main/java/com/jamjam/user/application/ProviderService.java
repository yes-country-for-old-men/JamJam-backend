package com.jamjam.user.application;

import com.jamjam.global.exception.ApiException;
import com.jamjam.user.domain.entity.*;
import com.jamjam.user.domain.repository.ProviderRepository;
import com.jamjam.user.domain.repository.UserRepository;
import com.jamjam.user.exception.UserError;
import com.jamjam.user.presentation.dto.request.ProviderRequest;
import com.jamjam.user.presentation.dto.response.ProviderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderService {
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createProvider(Long userId, ProviderRequest request) {
        log.info("[createProvider] userId={}, request={}", userId, request);
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("[createProvider] User not found: {}", userId);
                return new IllegalArgumentException("User not found");
            });
        log.info("[createProvider] UserEntity found: {}", user.getId());
        ProviderEntity entity = mapToEntity(user, request);
        log.info("[createProvider] ProviderEntity mapped: {}", entity);
        log.info("[createProvider] skills: {}", entity.getSkills());
        log.info("[createProvider] careers: {}", entity.getCareers());
        log.info("[createProvider] educations: {}", entity.getEducations());
        log.info("[createProvider] licenses: {}", entity.getLicenses());
        try {
            providerRepository.save(entity);
            log.info("[createProvider] ProviderEntity saved successfully");
        } catch (Exception e) {
            log.error("[createProvider] Error saving ProviderEntity", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Optional<ProviderResponse> getProvider(Long id) {
        log.info("[getProvider] id={}", id);
        return providerRepository.findById(id).map(this::mapToResponse);
    }

    @Transactional
    public ProviderEntity updateProvider(Long id, ProviderRequest request) {
        log.info("[updateProvider] id={}, request={}", id, request);
        ProviderEntity entity = providerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));

        log.info("[updateProvider] ProviderEntity found: {}", entity);

        if (entity.getContactHoursEnd() >= 24 || entity.getContactHoursEnd() <= 0) {
            throw new ApiException(UserError.INVALID_CONTACT_TIME);
        } else if (entity.getContactHoursStart() >= 24 || entity.getContactHoursStart() <= 0 ) {
            throw new ApiException(UserError.INVALID_CONTACT_TIME);
        }

        entity.updatePartial(
                request.categoryId(),
                request.location(),
                request.introduction(),
                request.contactHoursStart(),
                request.contactHoursEnd(),
                request.averageResponseTime()
        );

        if (request.skills() != null) {
            List<SkillEntity> updatedSkills = request.skills().stream().map(skillDto -> {
                SkillEntity skill = null;
                if (skillDto.id() != null) {
                    skill = entity.getSkills().stream()
                            .filter(s -> s.getId().equals(skillDto.id()))
                            .findFirst()
                            .orElse(null);
                }
                if (skill == null) {
                    skill = SkillEntity.builder()
                            .name(skillDto.name())
                            .provider(entity)
                            .build();
                } else {
                    skill.updatePartial(skillDto.name());
                }
                return skill;
            }).collect(Collectors.toList());
            entity.updateSkills(updatedSkills);
            log.info("[updateProvider] skills updated: {}", updatedSkills);
        }

        if (request.careers() != null) {
            List<CareerEntity> updatedCareers = request.careers().stream().map(careerDto -> {
                CareerEntity career = null;
                if (careerDto.id() != null) {
                    career = entity.getCareers().stream()
                            .filter(c -> c.getId().equals(careerDto.id()))
                            .findFirst()
                            .orElse(null);
                }
                if (career == null) {
                    career = CareerEntity.builder()
                            .companyName(careerDto.company())
                            .position(careerDto.position())
                            .department(careerDto.department())
                            .startDate(careerDto.startDate())
                            .endDate(careerDto.endDate())
                            .freelancer(careerDto.freelancer())
                            .proofUrl(careerDto.proofUrl())
                            .provider(entity)
                            .build();
                } else {
                    career.updatePartial(
                            careerDto.company(),
                            careerDto.position(),
                            careerDto.department(),
                            careerDto.startDate(),
                            careerDto.endDate(),
                            careerDto.freelancer(),
                            careerDto.proofUrl()
                    );
                }
                return career;
            }).collect(Collectors.toList());
            entity.updateCareers(updatedCareers);
            log.info("[updateProvider] careers updated: {}", updatedCareers);
        }

        if (request.educations() != null) {
            List<EducationEntity> updatedEducations = request.educations().stream().map(educationDto -> {
                EducationEntity education = null;
                if (educationDto.id() != null) {
                    education = entity.getEducations().stream()
                            .filter(e -> e.getId().equals(educationDto.id()))
                            .findFirst()
                            .orElse(null);
                }
                if (education == null) {
                    education = EducationEntity.builder()
                            .school(educationDto.school())
                            .major(educationDto.major())
                            .degree(educationDto.degree())
                            .provider(entity)
                            .build();
                } else {
                    education.updatePartial(
                            educationDto.school(),
                            educationDto.major(),
                            educationDto.degree()
                    );
                }
                return education;
            }).collect(Collectors.toList());
            entity.updateEducations(updatedEducations);
            log.info("[updateProvider] educations updated: {}", updatedEducations);
        }

        if (request.licenses() != null) {
            List<LicenseEntity> updatedLicenses = request.licenses().stream().map(licenseDto -> {
                LicenseEntity license = null;
                if (licenseDto.id() != null) {
                    license = entity.getLicenses().stream()
                            .filter(l -> l.getId().equals(licenseDto.id()))
                            .findFirst()
                            .orElse(null);
                }
                if (license == null) {
                    license = LicenseEntity.builder()
                            .name(licenseDto.name())
                            .provider(entity)
                            .build();
                } else {
                    license.updatePartial(licenseDto.name());
                }
                return license;
            }).collect(Collectors.toList());
            entity.updateLicenses(updatedLicenses);
            log.info("[updateProvider] licenses updated: {}", updatedLicenses);
        }

        log.info("[updateProvider] entity fully updated");
        return entity;
    }



    @Transactional
    public void deleteProvider(Long id) {
        log.info("[deleteProvider] id={}", id);
        providerRepository.deleteById(id);
        log.info("[deleteProvider] deleted");
    }

    private ProviderEntity mapToEntity(UserEntity user, ProviderRequest request) {
        log.info("[mapToEntity] user={}, request={}", user, request);
        List<SkillEntity> skills = request.skills() == null ? new ArrayList<>() :
                request.skills().stream().map(skillDto -> SkillEntity.builder().name(skillDto.name()).provider(null).build()).collect(Collectors.toList());
        List<CareerEntity> careers = request.careers() == null ? new ArrayList<>() :
                request.careers().stream().map(careerDto -> CareerEntity.builder()
                    .companyName(careerDto.company())
                    .position(careerDto.position())
                    .department(careerDto.department())
                    .startDate(careerDto.startDate())
                    .endDate(careerDto.endDate())
                    .freelancer(careerDto.freelancer())
                    .proofUrl(careerDto.proofUrl())
                    .provider(null)
                    .build()
                ).collect(Collectors.toList());
        List<EducationEntity> educations = request.educations() == null ? new ArrayList<>() :
                request.educations().stream().map(educationDto -> EducationEntity.builder().school(educationDto.school()).major(educationDto.major()).degree(educationDto.degree()).provider(null).build()).collect(Collectors.toList());
        List<LicenseEntity> licenses = request.licenses() == null ? new ArrayList<>() :
                request.licenses().stream().map(licenseDto -> LicenseEntity.builder().name(licenseDto.name()).provider(null).build()).collect(Collectors.toList());
        log.info("[mapToEntity] skills: {}", skills);
        log.info("[mapToEntity] careers: {}", careers);
        log.info("[mapToEntity] educations: {}", educations);
        log.info("[mapToEntity] licenses: {}", licenses);
        ProviderEntity entity = ProviderEntity.builder()
                .user(user)
                .categoryId(request.categoryId())
                .location(request.location())
                .introduction(request.introduction())
                .contactHoursStart(request.contactHoursStart())
                .contactHoursEnd(request.contactHoursEnd())
                .averageResponseTime(request.averageResponseTime())
                .skills(skills)
                .careers(careers)
                .educations(educations)
                .licenses(licenses)
                .build();
        log.info("[mapToEntity] ProviderEntity built: {}", entity);
        return entity;
    }

    private ProviderResponse mapToResponse(ProviderEntity entity) {
        log.info("[mapToResponse] entity={}", entity);
        return ProviderResponse.builder()
                .categoryId(entity.getCategoryId())
                .location(entity.getLocation())
                .introduction(entity.getIntroduction())
                .contactHoursStart(entity.getContactHoursStart())
                .contactHoursEnd(entity.getContactHoursEnd())
                .averageResponseTime(entity.getAverageResponseTime())
                .skills(entity.getSkills() == null ? List.of() : entity.getSkills().stream().map(s -> ProviderResponse.SkillDto.builder().id(s.getId()).name(s.getName()).build()).collect(Collectors.toList()))
                .careers(entity.getCareers() == null ? List.of() : entity.getCareers().stream().map(c -> ProviderResponse.CareerDto.builder().id(c.getId()).company(c.getCompanyName()).position(c.getPosition()).department(c.getDepartment()).startDate(c.getStartDate()).endDate(c.getEndDate()).freelancer(c.getFreelancer()).proofUrl(c.getProofUrl()).build()).collect(Collectors.toList()))
                .educations(entity.getEducations() == null ? List.of() : entity.getEducations().stream().map(e -> ProviderResponse.EducationDto.builder().id(e.getId()).school(e.getSchool()).major(e.getMajor()).degree(e.getDegree()).build()).collect(Collectors.toList()))
                .licenses(entity.getLicenses() == null ? List.of() : entity.getLicenses().stream().map(l -> ProviderResponse.LicenseDto.builder().id(l.getId()).name(l.getName()).build()).collect(Collectors.toList()))
                .build();
    }
}