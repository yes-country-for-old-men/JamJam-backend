package com.jamjam.user.application;

import com.jamjam.global.exception.ApiException;
import com.jamjam.infra.jwt.application.JwtUtil;
import com.jamjam.infra.jwt.domain.entity.RefreshEntity;
import com.jamjam.infra.jwt.domain.repository.RefreshRepository;
import com.jamjam.service.util.S3Uploader;
import com.jamjam.user.domain.entity.UserEntity;
import com.jamjam.user.domain.entity.UserRole;
import com.jamjam.user.domain.repository.UserRepository;
import com.jamjam.user.exception.UserError;
import com.jamjam.user.presentation.dto.request.ClientJoinRequest;
import com.jamjam.user.presentation.dto.request.ProviderJoinRequest;
import com.jamjam.user.presentation.dto.request.UserUpdateRequest;
import com.jamjam.user.presentation.dto.response.CheckCorrectResponse;
import com.jamjam.user.presentation.dto.response.CheckDuplicateResponse;
import com.jamjam.user.presentation.dto.response.LoginResponse;
import com.jamjam.user.presentation.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RefreshRepository refreshRepository;
    private final JwtUtil jwtUtil;
    private final S3Uploader s3Uploader;

    public UserResponse getUserInfo(Long userId) {
        Optional<UserEntity> userEntityOptional = userRepository.findById(userId);

        UserEntity userEntity = userEntityOptional.orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        return UserResponse.builder()
                .name(userEntity.getName())
                .loginId(userEntity.getLoginId())
                .phoneNumber(userEntity.getPhoneNumber())
                .nickname(userEntity.getNickname())
                .birth(userEntity.getBirth())
                .profileUrl(userEntity.getProfileUrl())
                .gender(userEntity.getGender())
                .role(userEntity.getRole())
                .credit(userEntity.getCredit())
                .build();
    }

    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequest request, MultipartFile profile) throws IOException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        if (request.deleteProfileImage()){
            s3Uploader.delete(user.getProfileUrl());
        }

        if (profile != null && !profile.isEmpty()) {
            String profileUrl = s3Uploader.upload(profile, "profile");
            log.info("프로필 사진 저장 완료: " + profileUrl);
            user.changeProfileUrl(profileUrl);
        }

        if (request.name()            != null) user.changeName(request.name());
        if (request.nickname()        != null) user.changeNickname(request.nickname());
        if (request.phoneNumber()     != null) user.changePhone(request.phoneNumber());
        if (request.birth()           != null) user.changeBirth(request.birth());
        if (request.gender()          != null) user.changeGender(request.gender());
    }

    @Transactional
    public LoginResponse joinProvider(ProviderJoinRequest request, HttpServletResponse response, String clientType) {

        if (validateDuplicateLoginId(request.loginId())) throw new ApiException(UserError.ID_ALREADY_EXISTS);

        UserEntity savedUser = saveProvider(request);

        String role = savedUser.getRole().toString();
        Long userId = savedUser.getId();

        String accessToken;

        if ("APP".equalsIgnoreCase(clientType)) {
            accessToken  = jwtUtil.generateToken("access",  userId, role, 60 * 60 * 24 * 7 * 1000L);

        } else {
            accessToken  = jwtUtil.generateToken("access",  userId, role, 60 * 60 * 1000L);
            String refreshToken = jwtUtil.generateToken("refresh", userId, role, 60 * 60 * 24 * 1000L);

            saveRefreshEntity(userId, refreshToken);

            addHeader(response, refreshToken);

        }
        return new LoginResponse(accessToken, "Bearer", 60 * 60 * 24);
    }

    @Transactional
    public LoginResponse joinClient(ClientJoinRequest request, HttpServletResponse response, String clientType) {

        if (validateDuplicateLoginId(request.loginId())) throw new ApiException(UserError.ID_ALREADY_EXISTS);

        UserEntity savedUser = saveClient(request);

        String role = savedUser.getRole().toString();
        Long userId = savedUser.getId();

        String accessToken;

        if ("APP".equalsIgnoreCase(clientType)) {
            accessToken  = jwtUtil.generateToken("access",  userId, role, 60 * 60 * 24 * 7 * 1000L);

        } else {
            accessToken  = jwtUtil.generateToken("access",  userId, role, 60 * 60 * 1000L);
            String refreshToken = jwtUtil.generateToken("refresh", userId, role, 60 * 60 * 24 * 1000L);

            saveRefreshEntity(userId, refreshToken);

            addHeader(response, refreshToken);

        }
        return new LoginResponse(accessToken, "Bearer", 60 * 60 * 24);
    }

    protected boolean validateDuplicateLoginId(String loginId) {
        return userRepository.findByLoginId(loginId).isPresent();
    }

    protected UserEntity saveClient(ClientJoinRequest request) {
        if (userRepository.existsByPhoneNumberAndRole(request.phoneNumber(), UserRole.CLIENT)){
            throw new ApiException(UserError.PHONE_ALREADY_REGISTERED);
        }
        return userRepository.save(
                UserEntity.builder()
                        .name(request.name())
                        .phoneNumber(request.phoneNumber())
                        .nickname(request.nickname())
                        .loginId(request.loginId())
                        .password(bCryptPasswordEncoder.encode(request.password()))
                        .birth(request.birth())
                        .gender(request.gender())
                        .createAt(LocalDate.now())
                        .role(UserRole.CLIENT)
                        .build()
        );
    }

    protected UserEntity saveProvider(ProviderJoinRequest request) {
        if (userRepository.existsByPhoneNumberAndRole(request.phoneNumber(), UserRole.PROVIDER)){
            throw new ApiException(UserError.PHONE_ALREADY_REGISTERED);
        }
        return userRepository.save(
                UserEntity.builder()
                        .name(request.name())
                        .phoneNumber(request.phoneNumber())
                        .nickname(request.nickname())
                        .loginId(request.loginId())
                        .password(bCryptPasswordEncoder.encode(request.password()))
                        .birth(request.birth())
                        .gender(request.gender())
                        .createAt(LocalDate.now())
                        .role(UserRole.PROVIDER)
                        .build()
        );
    }

    protected void addHeader(HttpServletResponse response, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                ResponseCookie.from("refresh", refreshToken)
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("None")
                        .path("/")
                        .maxAge(Duration.ofDays(1))
                        .build()
                        .toString());

    }

    protected void saveRefreshEntity(Long userId, String refreshToken) {
        refreshRepository.save(
                RefreshEntity.builder()
                        .userId(userId)
                        .refresh(refreshToken)
                        .expires(jwtUtil.getExpiration(refreshToken))
                        .build()
        );
    }

    public CheckDuplicateResponse checkDuplicateLoginId(String loginId) {
        return new CheckDuplicateResponse(!userRepository.existsByLoginId(loginId));
    }

    public CheckDuplicateResponse checkDuplicateNickName(String nickname) {
        return new CheckDuplicateResponse(!userRepository.existsByNickname(nickname));
    }

    public CheckCorrectResponse checkCorrectPassword(Long userId, String password) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));
        boolean matches = bCryptPasswordEncoder.matches(password, user.getPassword());
        return new CheckCorrectResponse(matches);
    }
}
