package com.jamjam.user.domain.repository;

import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByLoginId(String loginId);
    Optional<UserEntity> findByid(Long userId);
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    Boolean existsByLoginId(String loginId);
    Boolean existsByPhoneNumberAndRole(String phoneNumber, UserRole userRole);
    Boolean existsByNickname(String nickName);
    Optional<UserEntity> findByNameAndBirthAndPhoneNumber(String name, LocalDate birth, String phoneNumber);
    Optional<UserEntity> findByLoginIdAndNameAndBirthAndPhoneNumber(String loginId, String name, LocalDate birth, String phoneNumber);
}