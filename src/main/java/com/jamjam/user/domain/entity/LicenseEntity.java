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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private ProviderEntity provider;

    @Builder
    public LicenseEntity(String name, ProviderEntity provider, String proofUrl) {
        this.name = name;
        this.provider = provider;
        this.proofUrl = proofUrl;
    }

    public void updatePartial(String name, String proofUrl) {
        if (name != null) this.name = name;
        if (proofUrl != null) this.proofUrl = proofUrl;
    }
} 