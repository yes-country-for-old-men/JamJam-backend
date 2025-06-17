package com.jamjam.service.entity;

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
@Table(name="services")
@Builder
public class Service {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String title;

    @Lob
    @Column(name = "thumbnail", columnDefinition = "BYTEA")
    private byte[] thumbnail;

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
