package com.jamjam.service.entity;

import com.jamjam.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="services")
public class ServiceEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String title;
    private String thumbnail;

    @ElementCollection
    @CollectionTable(name = "service_info_images")
    private List<String> infoImages;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer salary;

    private Integer category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
