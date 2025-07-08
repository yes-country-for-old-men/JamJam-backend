package com.jamjam.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "career")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CareerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String company;

    private String position;
    private String proofUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private ProviderEntity provider;

    public void updatePartial(String company, String position, String proofUrl) {
        if (company != null) {
            this.company = company;
        }
        if (position != null) {
            this.position = position;
        }
        if (proofUrl != null) {
            this.proofUrl = proofUrl;
        }
    }
}
