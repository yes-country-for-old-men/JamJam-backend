package com.jamjam.user.domain.entity;

import lombok.Builder;

@Builder
public record AccountDto(
    String bankCode,
    String bankName,
    String accountNumber,
    String depositor
) {
    public static AccountDto fromEntity(AccountEntity entity) {
        if (entity == null || entity.getBank() == null) return null;
        return AccountDto.builder()
            .bankCode(entity.getBank().getCode())
            .bankName(entity.getBank().getName())
            .accountNumber(entity.getAccountNumber())
            .depositor(entity.getDepositor())
            .build();
    }
}
