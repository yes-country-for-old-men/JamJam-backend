package com.jamjam.user.application;

import com.jamjam.global.exception.ApiException;
import com.jamjam.service.util.S3Uploader;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public void createProvider(Long userId, ProviderRequest request,
                               List<MultipartFile> skillFiles,
                               List<MultipartFile> careerFiles,
                               List<MultipartFile> educationFiles,
                               List<MultipartFile> licenseFiles
    ) throws IOException {

        log.info("[createProvider] request: {}", request);

        List<SkillEntity> skills = new ArrayList<>();
        if (request.skills() != null) {
            for (int i = 0; i < request.skills().size(); i++) {
                ProviderRequest.SkillDto skillDto = request.skills().get(i);
                String proofUrl = null;

                if (skillFiles != null && skillFiles.size() > i && !skillFiles.get(i).isEmpty()) {
                    proofUrl = s3Uploader.upload(skillFiles.get(i), "skills");
                }

                SkillEntity skill = SkillEntity.builder()
                        .name(skillDto.name())
                        .proofUrl(proofUrl)
                        .clientSkillId(skillDto.id())
                        .build();
                skills.add(skill);
            }
        }

        // careers proof 파일 매핑
        List<CareerEntity> careers = new ArrayList<>();
        if (request.careers() != null) {
            for (int i = 0; i < request.careers().size(); i++) {
                ProviderRequest.CareerDto careerDto = request.careers().get(i);
                String proofUrl = null;

                if (careerFiles != null && careerFiles.size() > i && !careerFiles.get(i).isEmpty()) {
                    proofUrl = s3Uploader.upload(careerFiles.get(i), "careers");
                }

                CareerEntity career = CareerEntity.builder()
                        .company(careerDto.company())
                        .position(careerDto.position())
                        .proofUrl(proofUrl)
                        .clientCareerId(careerDto.id())
                        .build();
                careers.add(career);
            }
        }

        // educations proof 파일 매핑
        List<EducationEntity> educations = new ArrayList<>();
        if (request.educations() != null) {
            for (int i = 0; i < request.educations().size(); i++) {
                ProviderRequest.EducationDto educationDto = request.educations().get(i);
                String proofUrl = null;

                if (educationFiles != null && educationFiles.size() > i && !educationFiles.get(i).isEmpty()) {
                    proofUrl = s3Uploader.upload(educationFiles.get(i), "educations");
                }

                EducationEntity education = EducationEntity.builder()
                        .school(educationDto.school())
                        .major(educationDto.major())
                        .degree(educationDto.degree())
                        .proofUrl(proofUrl)
                        .clientEducationId(educationDto.id())
                        .build();
                educations.add(education);
            }
        }

        // licenses proof 파일 매핑
        List<LicenseEntity> licenses = new ArrayList<>();
        if (request.licenses() != null) {
            for (int i = 0; i < request.licenses().size(); i++) {
                ProviderRequest.LicenseDto licenseDto = request.licenses().get(i);
                String proofUrl = null;

                if (licenseFiles != null && licenseFiles.size() > i && !licenseFiles.get(i).isEmpty()) {
                    proofUrl = s3Uploader.upload(licenseFiles.get(i), "licenses");
                }

                LicenseEntity license = LicenseEntity.builder()
                        .name(licenseDto.name())
                        .proofUrl(proofUrl)
                        .clientLicenseId(licenseDto.id())
                        .build();
                licenses.add(license);
            }
        }

        Optional<ProviderEntity> existing = providerRepository.findById(userId);
        if (existing.isPresent()) {
            throw new ApiException(UserError.PROVIDER_ALREADY_EXISTS);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        ProviderEntity provider = ProviderEntity.builder()
                .user(user)
                .categoryId(request.categoryId())
                .location(request.location())
                .introduction(request.introduction())
                .contactHours(mapToContactHours(request.contactHours()))
                .skills(skills)
                .careers(careers)
                .educations(educations)
                .licenses(licenses)
                .build();

        providerRepository.save(provider);
        log.info("[createProvider] ProviderEntity saved: {}", provider);
    }

    private ContactHours mapToContactHours(ProviderRequest.ContactHoursDto dto) {
        if (dto == null) return null;

        Integer startHour = dto.startHour();
        Integer endHour = dto.endHour();

        if (startHour != null && (startHour < 0 || startHour >= 24)) {
            throw new ApiException(UserError.INVALID_CONTACT_TIME);
        }
        if (endHour != null && (endHour < 0 || endHour >= 24)) {
            throw new ApiException(UserError.INVALID_CONTACT_TIME);
        }

        return new ContactHours(
                startHour != null ? startHour : 0,
                endHour != null ? endHour : 0
        );
    }

    @Transactional(readOnly = true)
    public ProviderResponse getProvider(Long id) {
        log.info("[getProvider] id={}", id);
        return providerRepository.findById(id)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Transactional
    public ProviderResponse updateProvider(Long id, ProviderRequest request,
                                         List<MultipartFile> skillFiles,
                                         List<MultipartFile> careerFiles,
                                         List<MultipartFile> educationFiles,
                                         List<MultipartFile> licenseFiles) throws IOException {

        log.info("[updateProvider] id={}, request={}", id, request);
        ProviderEntity entity = providerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));

        log.info("[updateProvider] ProviderEntity found: {}", entity);

        ContactHours updatedContactHours = entity.getContactHours();

        if (request.contactHours() != null) {
            ProviderRequest.ContactHoursDto dto = request.contactHours();
            Integer startHour = dto.startHour();
            Integer endHour = dto.endHour();

            if (startHour != null && (startHour < 0 || startHour >= 24)) {
                throw new ApiException(UserError.INVALID_CONTACT_TIME);
            }
            if (endHour != null && (endHour < 0 || endHour >= 24)) {
                throw new ApiException(UserError.INVALID_CONTACT_TIME);
            }

            updatedContactHours = new ContactHours(
                    startHour != null ? startHour : entity.getContactHours().startHour(),
                    endHour != null ? endHour : entity.getContactHours().endHour()
            );
        }

        entity.updatePartial(
                request.categoryId(),
                request.location(),
                request.introduction(),
                updatedContactHours
        );

        // ✅ skills update
        if (request.skills() != null) {
            for (int i = 0; i < request.skills().size(); i++) {
                ProviderRequest.SkillDto dto = request.skills().get(i);
                SkillEntity skill = entity.getSkills().stream()
                        .filter(s -> Objects.equals(s.getClientSkillId(), dto.id()))
                        .findFirst()
                        .orElse(null);

                String proofUrl = null;
                if (skillFiles != null && skillFiles.size() > i && !skillFiles.get(i).isEmpty()) {
                    proofUrl = s3Uploader.upload(skillFiles.get(i), "skills");
                }

                if (skill == null) {
                    skill = SkillEntity.builder()
                            .name(dto.name())
                            .proofUrl(proofUrl)
                            .provider(entity)
                            .clientSkillId(dto.id())
                            .build();
                    entity.getSkills().add(skill);
                } else {
                    skill.updatePartial(dto.name(), proofUrl, dto.id());
                }
            }
            log.info("[updateProvider] skills add/update done");
        }

        // ✅ careers update
        if (request.careers() != null) {
            for (int i = 0; i < request.careers().size(); i++) {
                ProviderRequest.CareerDto dto = request.careers().get(i);
                CareerEntity career = entity.getCareers().stream()
                        .filter(c -> Objects.equals(c.getClientCareerId(), dto.id()))
                        .findFirst()
                        .orElse(null);

                String proofUrl = null;
                if (careerFiles != null && careerFiles.size() > i && !careerFiles.get(i).isEmpty()) {
                    proofUrl = s3Uploader.upload(careerFiles.get(i), "careers");
                }

                if (career == null) {
                    career = CareerEntity.builder()
                            .company(dto.company())
                            .position(dto.position())
                            .proofUrl(proofUrl)
                            .provider(entity)
                            .clientCareerId(dto.id())
                            .build();
                    entity.getCareers().add(career);
                } else {
                    career.updatePartial(dto.company(), dto.position(), proofUrl, dto.id());
                }
            }
            log.info("[updateProvider] careers add/update done");
        }

        // ✅ educations update
        if (request.educations() != null) {
            for (int i = 0; i < request.educations().size(); i++) {
                ProviderRequest.EducationDto dto = request.educations().get(i);
                EducationEntity education = entity.getEducations().stream()
                        .filter(e -> Objects.equals(e.getClientEducationId(), dto.id()))
                        .findFirst()
                        .orElse(null);

                String proofUrl = null;
                if (educationFiles != null && educationFiles.size() > i && !educationFiles.get(i).isEmpty()) {
                    proofUrl = s3Uploader.upload(educationFiles.get(i), "educations");
                }

                if (education == null) {
                    education = EducationEntity.builder()
                            .school(dto.school())
                            .major(dto.major())
                            .degree(dto.degree())
                            .proofUrl(proofUrl)
                            .provider(entity)
                            .clientEducationId(dto.id())
                            .build();
                    entity.getEducations().add(education);
                } else {
                    education.updatePartial(dto.school(), dto.major(), dto.degree(), proofUrl, dto.id());
                }
            }
            log.info("[updateProvider] educations add/update done");
        }

        // ✅ licenses update
        if (request.licenses() != null) {
            for (int i = 0; i < request.licenses().size(); i++) {
                ProviderRequest.LicenseDto dto = request.licenses().get(i);
                LicenseEntity license = entity.getLicenses().stream()
                        .filter(l -> Objects.equals(l.getClientLicenseId(), dto.id()))
                        .findFirst()
                        .orElse(null);

                String proofUrl = null;
                if (licenseFiles != null && licenseFiles.size() > i && !licenseFiles.get(i).isEmpty()) {
                    proofUrl = s3Uploader.upload(licenseFiles.get(i), "licenses");
                }

                if (license == null) {
                    license = LicenseEntity.builder()
                            .name(dto.name())
                            .proofUrl(proofUrl)
                            .provider(entity)
                            .clientLicenseId(dto.id())
                            .build();
                    entity.getLicenses().add(license);
                } else {
                    license.updatePartial(dto.name(), proofUrl, dto.id());
                }
            }
            log.info("[updateProvider] licenses add/update done");
        }

        if (request.deletedSkillIds() != null && !request.deletedSkillIds().isEmpty()) {
            entity.getSkills().removeIf(skill -> request.deletedSkillIds().contains(skill.getClientSkillId()));
            log.info("[updateProvider] skills deleted: {}", request.deletedSkillIds());
        }
        if (request.deletedCareerIds() != null && !request.deletedCareerIds().isEmpty()) {
            entity.getCareers().removeIf(career -> request.deletedCareerIds().contains(career.getClientCareerId()));
            log.info("[updateProvider] careers deleted: {}", request.deletedCareerIds());
        }
        if (request.deletedEducationIds() != null && !request.deletedEducationIds().isEmpty()) {
            entity.getEducations().removeIf(education -> request.deletedEducationIds().contains(education.getClientEducationId()));
            log.info("[updateProvider] educations deleted: {}", request.deletedEducationIds());
        }
        if (request.deletedLicenseIds() != null && !request.deletedLicenseIds().isEmpty()) {
            entity.getLicenses().removeIf(license -> request.deletedLicenseIds().contains(license.getClientLicenseId()));
            log.info("[updateProvider] licenses deleted: {}", request.deletedLicenseIds());
        }

        log.info("[updateProvider] entity fully updated");
        return mapToResponse(entity);

    }


    @Transactional
    public void deleteProvider(Long id) {
        log.info("[deleteProvider] id={}", id);
        providerRepository.deleteById(id);
        log.info("[deleteProvider] deleted");
    }

    private ProviderEntity mapToEntity(UserEntity user, ProviderRequest request) {
        log.info("[mapToEntity] user={}, request={}", user, request);

        ContactHours contactHours = null;
        if (request.contactHours() != null) {
            ProviderRequest.ContactHoursDto dto = request.contactHours();
            Integer startHour = dto.startHour();
            Integer endHour = dto.endHour();

            if (startHour != null && (startHour < 0 || startHour >= 24)) {
                throw new ApiException(UserError.INVALID_CONTACT_TIME);
            }
            if (endHour != null && (endHour < 0 || endHour >= 24)) {
                throw new ApiException(UserError.INVALID_CONTACT_TIME);
            }

            contactHours = new ContactHours(
                    startHour != null ? startHour : 0,
                    endHour != null ? endHour : 0
            );
        }

        List<SkillEntity> skills = request.skills() == null ? new ArrayList<>() :
                request.skills().stream().map(skillDto -> SkillEntity.builder().name(skillDto.name()).provider(null).build()).collect(Collectors.toList());
        List<CareerEntity> careers = request.careers() == null ? new ArrayList<>() :
                request.careers().stream()
                        .map(careerDto -> CareerEntity.builder()
                                .company(careerDto.company())
                                .position(careerDto.position())
                                .build()
                        )
                        .collect(Collectors.toList());
        List<EducationEntity> educations = request.educations() == null ? new ArrayList<>() :
                request.educations().stream().map(educationDto -> EducationEntity.builder()
                        .school(educationDto.school())
                        .major(educationDto.major())
                        .degree(educationDto.degree())
                        .provider(null)
                        .build()
                ).collect(Collectors.toList());
        List<LicenseEntity> licenses = request.licenses() == null ? new ArrayList<>() :
                request.licenses().stream().map(licenseDto -> LicenseEntity.builder().name(licenseDto.name()).provider(null).build()).collect(Collectors.toList());

        ProviderEntity entity = ProviderEntity.builder()
                .user(user)
                .categoryId(request.categoryId())
                .location(request.location())
                .introduction(request.introduction())
                .contactHours(contactHours)
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

        ProviderResponse.ContactHoursDto contactHoursDto = null;
        if (entity.getContactHours() != null) {
            contactHoursDto = ProviderResponse.ContactHoursDto.builder()
                    .startHour(entity.getContactHours().startHour())
                    .endHour(entity.getContactHours().endHour())
                    .build();
        }

        return ProviderResponse.builder()
                .categoryId(entity.getCategoryId())
                .location(entity.getLocation())
                .introduction(entity.getIntroduction())
                .contactHours(contactHoursDto)
                .averageResponseTime(entity.getAverageResponseTime())
                .skills(entity.getSkills() == null ? List.of() :
                        entity.getSkills().stream()
                                .map(s -> ProviderResponse.SkillDto.builder()
                                        .id(s.getClientSkillId())
                                        .name(s.getName())
                                        .proofUrl(s.getProofUrl())
                                        .build())
                                .collect(Collectors.toList()))
                .careers(entity.getCareers() == null ? List.of() :
                        entity.getCareers().stream()
                                .map(c -> ProviderResponse.CareerDto.builder()
                                        .id(c.getClientCareerId())
                                        .company(c.getCompany())
                                        .position(c.getPosition())
                                        .proofUrl(c.getProofUrl())
                                        .build())
                                .collect(Collectors.toList()))
                .educations(entity.getEducations() == null ? List.of() :
                        entity.getEducations().stream()
                                .map(e -> ProviderResponse.EducationDto.builder()
                                        .id(e.getClientEducationId())
                                        .school(e.getSchool())
                                        .major(e.getMajor())
                                        .degree(e.getDegree())
                                        .proofUrl(e.getProofUrl())
                                        .build())
                                .collect(Collectors.toList()))
                .licenses(entity.getLicenses() == null ? List.of() :
                        entity.getLicenses().stream()
                                .map(l -> ProviderResponse.LicenseDto.builder()
                                        .id(l.getClientLicenseId())
                                        .name(l.getName())
                                        .proofUrl(l.getProofUrl())
                                        .build())
                                .collect(Collectors.toList()))
                .build();
    }
}