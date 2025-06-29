package com.jamjam.user.domain.repository;

import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByLoginId(String loginId);
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    Boolean existsByLoginId(String loginId);
    Boolean existsByNickname(String nickName);
    Boolean existsByPhoneNumberAndRole(String phoneNumber, UserRole userRole);
}