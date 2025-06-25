package com.jamjam.user.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "holder_name")
    private String holderName;

    @Builder
    public AccountEntity(String bankName, String accountNumber, String holderName) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.holderName = holderName;
    }
}