package com.jamjam.user.application;

import com.jamjam.global.exception.ApiException;
import com.jamjam.service.dto.ServiceSummaryDTO;
import com.jamjam.service.util.S3Uploader;
import com.jamjam.user.domain.entity.*;
import com.jamjam.user.domain.repository.ProviderRepository;
import com.jamjam.user.domain.repository.UserRepository;
import com.jamjam.user.exception.UserError;
import com.jamjam.user.presentation.dto.request.ProviderRequest;
import com.jamjam.user.presentation.dto.response.ProviderResponse;
import com.jamjam.user.presentation.dto.response.ProviderPageResponse;
import com.jamjam.service.domain.entity.ServiceEntity;
import com.jamjam.service.domain.repository.ServiceRepository;
import com.jamjam.service.dto.ServiceInfoDTO;
import org.springframework.data.domain.Pageable;
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
    private final ServiceRepository serviceRepository;

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

        if (startHour != null && (startHour < 0 || startHour > 24)) {
            throw new ApiException(UserError.INVALID_CONTACT_TIME);
        }
        if (endHour != null && (endHour < 0 || endHour > 24)) {
            throw new ApiException(UserError.INVALID_CONTACT_TIME);
        }

        return new ContactHours(
                startHour != null ? startHour : 1,
                endHour != null ? endHour : 24
        );
    }

    @Transactional(readOnly = true)
    public ProviderResponse getProvider(Long id) {
        log.info("[getProvider] id={}", id);
        return providerRepository.findById(id)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public ProviderPageResponse getProviderPage(Long userId) {
        ProviderEntity provider = providerRepository.findById(userId)
            .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        List<ServiceEntity> services = serviceRepository.findByUserId(userId, Pageable.unpaged()).getContent();
        List<ServiceSummaryDTO> serviceDTOs = services.stream()
                .map(ServiceSummaryDTO::from)
                .toList();

        return ProviderPageResponse.builder()
            .categoryId(provider.getCategoryId())
            .location(provider.getLocation())
            .introduction(provider.getIntroduction())
            .contactHours(provider.getContactHours() == null ? null : ProviderPageResponse.ContactHoursDto.builder()
                .startHour(provider.getContactHours().startHour())
                .endHour(provider.getContactHours().endHour())
                .build())
            .averageResponseTime(provider.getAverageResponseTime())
            .skills(provider.getSkills() == null ? List.of() :
                provider.getSkills().stream()
                    .map(s -> ProviderPageResponse.SkillDto.builder()
                        .id(s.getClientSkillId())
                        .name(s.getName())
                        .proofUrl(s.getProofUrl())
                        .build())
                    .toList())
            .careers(provider.getCareers() == null ? List.of() :
                provider.getCareers().stream()
                    .map(c -> ProviderPageResponse.CareerDto.builder()
                        .id(c.getClientCareerId())
                        .company(c.getCompany())
                        .position(c.getPosition())
                        .proofUrl(c.getProofUrl())
                        .build())
                    .toList())
            .educations(provider.getEducations() == null ? List.of() :
                provider.getEducations().stream()
                    .map(e -> ProviderPageResponse.EducationDto.builder()
                        .id(e.getClientEducationId())
                        .school(e.getSchool())
                        .major(e.getMajor())
                        .degree(e.getDegree())
                        .proofUrl(e.getProofUrl())
                        .build())
                    .toList())
            .licenses(provider.getLicenses() == null ? List.of() :
                provider.getLicenses().stream()
                    .map(l -> ProviderPageResponse.LicenseDto.builder()
                        .id(l.getClientLicenseId())
                        .name(l.getName())
                        .proofUrl(l.getProofUrl())
                        .build())
                    .toList())
            .profileUrl(provider.getUser().getProfileUrl())
            .nickname(provider.getUser().getNickname())
            .services(serviceDTOs)
            .build();
    }

    @Transactional
    public ProviderResponse updateProvider(Long id, ProviderRequest request,
                                         List<MultipartFile> skillFiles,
                                         List<MultipartFile> careerFiles,
                                         List<MultipartFile> educationFiles,
                                         List<MultipartFile> licenseFiles) throws IOException {

        log.info("[updateProvider] id={}, request={}", id, request);
        ProviderEntity entity = providerRepository.findById(id)
                .orElseThrow(() -> new ApiException(UserError.PROVIDER_NOT_FOUND));

        log.info("[updateProvider] ProviderEntity found: {}", entity);

        ContactHours updatedContactHours = entity.getContactHours();

        if (request.contactHours() != null) {
            ProviderRequest.ContactHoursDto dto = request.contactHours();
            Integer startHour = dto.startHour();
            Integer endHour = dto.endHour();

            if (startHour != null && (startHour < 0 || startHour > 24)) {
                throw new ApiException(UserError.INVALID_CONTACT_TIME);
            }
            if (endHour != null && (endHour < 0 || endHour > 24)) {
                throw new ApiException(UserError.INVALID_CONTACT_TIME);
            }

            Integer currentStartHour = entity.getContactHours() != null ? entity.getContactHours().startHour() : null;
            Integer currentEndHour = entity.getContactHours() != null ? entity.getContactHours().endHour() : null;

            updatedContactHours = new ContactHours(
                    startHour != null ? startHour : currentStartHour,
                    endHour != null ? endHour : currentEndHour
            );
        }

        entity.updatePartial(
                request.categoryId(),
                request.location(),
                request.introduction(),
                updatedContactHours
        );

        if (request.skills() != null) {
            entity.getSkills().clear();
            
            if (!request.skills().isEmpty()) {
                for (int i = 0; i < request.skills().size(); i++) {
                    ProviderRequest.SkillDto dto = request.skills().get(i);

                    String proofUrl = null;
                    if (skillFiles != null && skillFiles.size() > i && !skillFiles.get(i).isEmpty()) {
                        proofUrl = s3Uploader.upload(skillFiles.get(i), "skills");
                    }

                    SkillEntity skill = SkillEntity.builder()
                            .name(dto.name())
                            .proofUrl(proofUrl)
                            .provider(entity)
                            .clientSkillId(dto.id())
                            .build();
                    entity.getSkills().add(skill);
                }
            }
        }

        if (request.careers() != null) {
            entity.getCareers().clear();
            
            if (!request.careers().isEmpty()) {
                for (int i = 0; i < request.careers().size(); i++) {
                    ProviderRequest.CareerDto dto = request.careers().get(i);

                    String proofUrl = null;
                    if (careerFiles != null && careerFiles.size() > i && !careerFiles.get(i).isEmpty()) {
                        proofUrl = s3Uploader.upload(careerFiles.get(i), "careers");
                    }

                    CareerEntity career = CareerEntity.builder()
                                .company(dto.company())
                                .position(dto.position())
                                .proofUrl(proofUrl)
                                .provider(entity)
                                .clientCareerId(dto.id())
                                .build();
                    entity.getCareers().add(career);
                }
            }
        }

        if (request.educations() != null) {
            entity.getEducations().clear();
            
            if (!request.educations().isEmpty()) {
                for (int i = 0; i < request.educations().size(); i++) {
                    ProviderRequest.EducationDto dto = request.educations().get(i);

                    String proofUrl = null;
                    if (educationFiles != null && educationFiles.size() > i && !educationFiles.get(i).isEmpty()) {
                        proofUrl = s3Uploader.upload(educationFiles.get(i), "educations");
                    }

                    EducationEntity education = EducationEntity.builder()
                            .school(dto.school())
                            .major(dto.major())
                            .degree(dto.degree())
                            .proofUrl(proofUrl)
                            .provider(entity)
                            .clientEducationId(dto.id())
                            .build();
                    entity.getEducations().add(education);
                }
            }
        }

        if (request.licenses() != null) {
            entity.getLicenses().clear();
            
            if (!request.licenses().isEmpty()) {
                for (int i = 0; i < request.licenses().size(); i++) {
                    ProviderRequest.LicenseDto dto = request.licenses().get(i);

                    String proofUrl = null;
                    if (licenseFiles != null && licenseFiles.size() > i && !licenseFiles.get(i).isEmpty()) {
                        proofUrl = s3Uploader.upload(licenseFiles.get(i), "licenses");
                    }

                    LicenseEntity license = LicenseEntity.builder()
                                .name(dto.name())
                                .proofUrl(proofUrl)
                                .provider(entity)
                                .clientLicenseId(dto.id())
                                .build();
                        entity.getLicenses().add(license);
                }
            }
        }
        return mapToResponse(entity);
    }

    @Transactional
    public void deleteProvider(Long id) {
        log.info("[deleteProvider] id={}", id);
        providerRepository.deleteById(id);
        log.info("[deleteProvider] deleted");
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