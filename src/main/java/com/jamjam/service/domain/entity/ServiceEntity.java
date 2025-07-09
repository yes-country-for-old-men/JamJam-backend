package com.jamjam.service.domain.entity;

import com.jamjam.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="services")
public class ServiceEntity {
    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private String serviceName;
    @NotNull
    @Column(columnDefinition = "TEXT")
    private String thumbnail;
    @NotNull
    private Integer salary;
    @NotNull
    private Integer categoryId;

//    @ElementCollection
//    @CollectionTable(name = "service_info_images", joinColumns = @JoinColumn(name = "service_id"))
//    private List<String> portfolioImages;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceInfoImageEntity> portfolioImages = new ArrayList<>();
}
