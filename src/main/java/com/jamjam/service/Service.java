package com.jamjam.service;

import com.jamjam.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="service")
@Builder
public class Service {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String skills;

    private Integer salary;

    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
