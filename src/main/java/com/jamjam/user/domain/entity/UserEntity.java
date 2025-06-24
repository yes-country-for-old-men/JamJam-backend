package com.jamjam.user.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@Getter
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    @Getter
    private Long id;

    @NotNull
    private String name;

    @NotNull
    @Column(name = "login_id")
    private String loginId;

    @NotNull
    private String password;

    @NotNull
    private String nickname;

    @NotNull
    @Column(name = "phone_number")
    private String phoneNumber;

    @NotNull
    @Column(name = "is_phone_verified")
    private boolean isPhoneVerified;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birth;

    @NotNull
    @Column(name = "create_at")
    private LocalDate createAt;

    @NotNull
    private UserRole role;

    private String profileUrl;

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<CareerEntity> careers = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL
            , orphanRemoval = true,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountEntity account; // 어떻게할지

    @Builder(toBuilder = true)
    public UserEntity(
            String name, String phoneNumber, boolean isPhoneVerified,
            String loginId, String password, LocalDate birth,
            Gender gender, LocalDate createAt, UserRole role,
            String nickname) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.isPhoneVerified = isPhoneVerified;
        this.loginId = loginId;
        this.password = password;
        this.birth = birth;
        this.gender = gender;
        this.createAt = createAt;
        this.role = role;
        this.nickname = nickname;
        this.profileUrl = "";
        this.careers = new ArrayList<>();
    }

    public void changeName(String newName) {
        this.name = newName;
    }

    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void changeLoginId(String newLoginId) {
        this.loginId = newLoginId;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changePhone(String newPhoneNumber) {
        this.phoneNumber = newPhoneNumber;
    }

    public void changeVerifyPhone(boolean verified) {
        this.isPhoneVerified = verified;
    }

    public void changeBirth(LocalDate newBirth) {
        this.birth = newBirth;
    }

    public void changeGender(Gender newGender) {
        this.gender = newGender;
    }

    public void changeProfileUrl(String newProfileUrl) {
        this.profileUrl = newProfileUrl;
    }
}
