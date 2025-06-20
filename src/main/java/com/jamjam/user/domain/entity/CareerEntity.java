package com.jamjam.user.domain.entity;

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
    private UserEntity user;

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
                                  String proofUrl) {
        this.freelancer = freelancer;
        this.companyName = companyName;
        this.department = department;
        this.position = position;
        this.startDate = startDate;
        this.endDate = endDate;
        this.proofUrl = proofUrl;
    }
}