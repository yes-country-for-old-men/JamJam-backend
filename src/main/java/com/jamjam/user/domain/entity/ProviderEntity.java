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

    @Embedded
    private ContactHours contactHours;

    private String averageResponseTime;

    private Integer categoryId;

    private String location;

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
                          String introduction,
                          ContactHours contactHours,
                          Integer categoryId,
                          String location,
                          List<SkillEntity> skills,
                          List<CareerEntity> careers,
                          List<EducationEntity> educations,
                          List<LicenseEntity> licenses) {
        this.user = user;
        this.introduction = introduction;
        this.contactHours = contactHours;
        this.averageResponseTime = "24시간 이내";
        this.categoryId = categoryId;
        this.location = location;
        if (skills != null) this.skills = skills;
        if (careers != null) this.careers = careers;
        if (educations != null) this.educations = educations;
        if (licenses != null) this.licenses = licenses;
    }

    public void updatePartial(
            Integer categoryId,
            String location,
            String introduction,
            ContactHours contactHours
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
        if (contactHours != null) {
            this.contactHours = contactHours;
        }
    }

    public void updateSkills(List<SkillEntity> updatedSkills) {
        this.skills.clear();
        this.skills.addAll(updatedSkills);
    }

    public void updateCareers(List<CareerEntity> updatedCareers) {
        this.careers.clear();
        this.careers.addAll(updatedCareers);
    }

    public void updateEducations(List<EducationEntity> updatedEducations) {
        this.educations.clear();
        this.educations.addAll(updatedEducations);
    }

    public void updateLicenses(List<LicenseEntity> updatedLicenses) {
        this.licenses.clear();
        this.licenses.addAll(updatedLicenses);
    }
}
