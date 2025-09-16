package com.jamjam.user.application;

import com.jamjam.global.exception.ApiException;
import com.jamjam.infra.jwt.application.JwtUtil;
import com.jamjam.infra.jwt.domain.entity.RefreshEntity;
import com.jamjam.infra.jwt.domain.repository.RefreshRepository;
import com.jamjam.service.util.S3Uploader;
import com.jamjam.user.domain.entity.*;
import com.jamjam.user.domain.repository.CreditHistoryRepository;
import com.jamjam.user.domain.repository.UserRepository;
import com.jamjam.user.exception.UserError;
import com.jamjam.user.presentation.dto.request.ClientJoinRequest;
import com.jamjam.user.presentation.dto.request.ProviderJoinRequest;
import com.jamjam.user.presentation.dto.request.UserUpdateRequest;
import com.jamjam.user.presentation.dto.response.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
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
    private final CreditHistoryRepository creditHistoryRepository;

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
                .account(AccountDto.fromEntity(userEntity.getAccount()))
                .build();
    }

    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequest request, MultipartFile profile) throws IOException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(request.deleteProfileImage())) {
            s3Uploader.delete(user.getProfileUrl());
            log.info("프로필 사진 삭제");
            user.changeProfileUrl(null);

        } else if (profile != null && !profile.isEmpty()) {
            String url = s3Uploader.upload(profile, "profile");
            log.info("프로필 사진 업로드: {}", url);
            user.changeProfileUrl(url);

        } else {
            log.debug("프로필 사진 변경 없음");
        }

        if (request.nickname()        != null) user.changeNickname(request.nickname());
        if (request.phoneNumber()     != null) user.changePhone(request.phoneNumber());
        if (request.password()       != null) user.changePassword(bCryptPasswordEncoder.encode(request.password()));
        if (request.account() != null) {
            AccountDto requestAccount = request.account();
            if (user.getAccount() != null) {
                if (requestAccount.accountNumber() != null) user.getAccount().changeAccountNumber(requestAccount.accountNumber());
                if (requestAccount.depositor()  != null) user.getAccount().changeDepositor(requestAccount.depositor());
                if (requestAccount.bankCode() != null) user.getAccount().changeBank(BankType.fromCode(requestAccount.bankCode()));
            } else if (requestAccount.bankCode() != null && requestAccount.accountNumber() != null && requestAccount.depositor() != null) {
                user.registerAccount(new AccountEntity(BankType.fromCode(requestAccount.bankCode()), requestAccount.accountNumber(), requestAccount.depositor()));
            }
        }
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
    /*유저의 크레딧 사용 내역 반환
    * 분류: 전체, 입금, 출금*/
    public CreditHistoryResponse getCreditHistory(Long userId, CreditChangeType type, Pageable pageable) {
        Page<CreditHistoryEntity> entities;
        if (type == CreditChangeType.ALL) {
            entities = creditHistoryRepository.findByUserId(userId, pageable);
        } else {
            entities = creditHistoryRepository.findByUserIdAndType(userId, type, pageable);
        }

        List<CreditHistorySummary> dtoList = entities.stream()
                .map(CreditHistorySummary::from)
                .toList();

        return CreditHistoryResponse.builder()
                .histories(dtoList)
                .currentPage(entities.getNumber())
                .totalPages(entities.getTotalPages())
                .hasNext(entities.hasNext())
                .build();
    }
}
