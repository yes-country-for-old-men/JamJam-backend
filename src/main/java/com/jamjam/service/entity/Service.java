package com.jamjam.service.entity;

import com.jamjam.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="services")
@Builder
public class Service {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String title;
    private String thumbnail;

    @ElementCollection
    private List<String> infoImages;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer salary;

    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
