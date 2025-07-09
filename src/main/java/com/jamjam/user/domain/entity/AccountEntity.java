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

    private BankType bank;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "holder_name")
    private String depositor;

    @Builder
    public AccountEntity(BankType bank, String accountNumber, String depositor) {
        this.bank = bank;
        this.accountNumber = accountNumber;
        this.depositor = depositor;
    }

    public void changeAccountNumber(String newAccountNumber) { this.accountNumber = newAccountNumber; }

    public void changeDepositor(String newDepositor) { this.depositor = newDepositor; }

    public void changeBank(BankType newBank) { this.bank = newBank; }
}