package com.jamjam.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "sms_verification")
public class SmsVerificationEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column @NotNull
    private String phoneNumber;

    @Column @NotNull
    private String verificationCode;

    @Column @NotNull
    private LocalDateTime createdAt;

    @Column @NotNull
    private LocalDateTime expiresAt;

    @Builder(toBuilder = true)
    public SmsVerificationEntity(String phoneNumber, String verificationCode, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}
