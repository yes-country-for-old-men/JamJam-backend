package com.jamjam.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "provider")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProviderEntity {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(length = 3000)
    private String introduction;

    private String contactHours;

    private String averageResponseTime;

    private Integer categoryId;

    private String location;

    private Integer contactHoursStart;

    private Integer contactHoursEnd;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private List<SkillEntity> skills = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private List<CareerEntity> careers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private List<EducationEntity> educations = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private List<LicenseEntity> licenses = new ArrayList<>();

    @Builder
    public ProviderEntity(UserEntity user,
                          String introduction, String contactHours,
                          String averageResponseTime, Integer categoryId,
                          String location, Integer contactHoursStart,
                          Integer contactHoursEnd,
                          List<SkillEntity> skills, List<CareerEntity> careers,
                          List<EducationEntity> educations, List<LicenseEntity> licenses
    ) {
        this.user = user;
        this.introduction = introduction;
        this.contactHours = contactHours;
        this.averageResponseTime = averageResponseTime;
        this.categoryId = categoryId;
        this.location = location;
        this.contactHoursStart = contactHoursStart;
        this.contactHoursEnd = contactHoursEnd;
        if (skills != null) this.skills = skills;
        if (careers != null) this.careers = careers;
        if (educations != null) this.educations = educations;
        if (licenses != null) this.licenses = licenses;
    }

    public void updatePartial(
            Integer categoryId,
            String location,
            String introduction,
            Integer contactHoursStart,
            Integer contactHoursEnd,
            String averageResponseTime
    ) {
        if (categoryId != null) {
            this.categoryId = categoryId;
        }
        if (location != null) {
            this.location = location;
        }
        if (introduction != null) {
            this.introduction = introduction;
        }
        if (contactHoursStart != null) {
            this.contactHoursStart = contactHoursStart;
        }
        if (contactHoursEnd != null) {
            this.contactHoursEnd = contactHoursEnd;
        }
        if (averageResponseTime != null) {
            this.averageResponseTime = averageResponseTime;
        }
    }

    // ✅ skills update method
    public void updateSkills(List<SkillEntity> updatedSkills) {
        this.skills.clear();
        this.skills.addAll(updatedSkills);
    }

    // ✅ careers update method
    public void updateCareers(List<CareerEntity> updatedCareers) {
        this.careers.clear();
        this.careers.addAll(updatedCareers);
    }

    // ✅ educations update method
    public void updateEducations(List<EducationEntity> updatedEducations) {
        this.educations.clear();
        this.educations.addAll(updatedEducations);
    }

    // ✅ licenses update method
    public void updateLicenses(List<LicenseEntity> updatedLicenses) {
        this.licenses.clear();
        this.licenses.addAll(updatedLicenses);
    }
}
