package com.jamjam.user;

import com.jamjam.service.entity.Service;
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
@Table(name="users")
@Builder
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String name;

    private String phone;

    private String email;

    @Enumerated(EnumType.STRING)
    private Role role; // provider or consumer

    private String bankName;

    private String accountHolder;

    private String accountNumber;

    private Boolean isAccountVerified;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Service> services;
}
