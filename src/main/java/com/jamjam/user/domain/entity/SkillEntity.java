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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private ProviderEntity provider;

    @Builder
    public SkillEntity(String name, ProviderEntity provider) {
        this.name = name;
        this.provider = provider;
    }

    public void updatePartial(String name) {
        if (name != null) this.name = name;
    }
} 