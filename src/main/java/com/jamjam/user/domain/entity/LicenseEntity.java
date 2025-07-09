package com.jamjam.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "provider_license")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LicenseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String proofUrl;
    private Long clientLicenseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private ProviderEntity provider;

    @Builder
    public LicenseEntity(String name, ProviderEntity provider, String proofUrl, Long clientLicenseId) {
        this.name = name;
        this.provider = provider;
        this.proofUrl = proofUrl;
        this.clientLicenseId = clientLicenseId;
    }

    public void updatePartial(String name, String proofUrl, Long clientLicenseId) {
        if (name != null) this.name = name;
        if (proofUrl != null) this.proofUrl = proofUrl;
        if (clientLicenseId != null) this.clientLicenseId = clientLicenseId;
    }
} 