package com.jamjam.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "provider_skill")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String proofUrl;

    private Long clientSkillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private ProviderEntity provider;

    @Builder
    public SkillEntity(
            String name, ProviderEntity provider,
            String proofUrl, Long clientSkillId) {
        this.name = name;
        this.proofUrl = proofUrl;
        this.provider = provider;
        this.clientSkillId = clientSkillId;
    }
} 