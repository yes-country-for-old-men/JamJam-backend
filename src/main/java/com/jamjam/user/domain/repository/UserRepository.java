package com.jamjam.user.domain.repository;

import com.jamjam.user.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByLoginId(String loginId);
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    Boolean existsByLoginId(String loginId);
    Boolean existsByPhoneNumberAndRole(String phoneNumber, UserRole userRole);
    Boolean existsByNickname(String nickName);
}
