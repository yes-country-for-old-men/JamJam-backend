package com.jamjam.service.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "service_info_images")
@Getter
@Setter
@NoArgsConstructor
public class ServiceInfoImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceEntity service;

    @Builder
    public ServiceInfoImageEntity(String imageUrl, ServiceEntity service) {
        this.imageUrl = imageUrl;
        this.service = service;
    }
}
