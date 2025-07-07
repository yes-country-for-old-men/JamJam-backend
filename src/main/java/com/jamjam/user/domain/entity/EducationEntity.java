package com.jamjam.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "provider_education")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EducationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String school;
    private String major;
    private String degree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private ProviderEntity provider;

    @Builder
    public EducationEntity(String school, String major, String degree, ProviderEntity provider) {
        this.school = school;
        this.major = major;
        this.degree = degree;
        this.provider = provider;
    }

    public void updatePartial(String school, String major, String degree) {
        if (school != null) this.school = school;
        if (major != null) this.major = major;
        if (degree != null) this.degree = degree;
    }
} 