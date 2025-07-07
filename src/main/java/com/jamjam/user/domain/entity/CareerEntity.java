package com.jamjam.user.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "career")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private ProviderEntity provider;

    @Column(name = "company_name")
    private String companyName;

    private String department;

    private String position;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean freelancer;

    private String proofUrl;

    @Builder
    public CareerEntity(Boolean freelancer,
                       String companyName,
                       String department,
                       String position,
                       LocalDate startDate,
                       LocalDate endDate,
                       String proofUrl,
                       ProviderEntity provider) {
        this.freelancer = freelancer;
        this.companyName = companyName;
        this.department = department;
        this.position = position;
        this.startDate = startDate;
        this.endDate = endDate;
        this.proofUrl = proofUrl;
        this.provider = provider;
    }

    public static CareerEntity of(String company, String position) {
        CareerEntity ce = new CareerEntity();
        ce.companyName = company;
        ce.position = position;
        return ce;
    }

    public void updatePartial(String companyName, String position, String department, LocalDate startDate, LocalDate endDate, Boolean freelancer, String proofUrl) {
        if (companyName != null) this.companyName = companyName;
        if (position != null) this.position = position;
        if (department != null) this.department = department;
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
        if (freelancer != null) this.freelancer = freelancer;
        if (proofUrl != null) this.proofUrl = proofUrl;
    }
}